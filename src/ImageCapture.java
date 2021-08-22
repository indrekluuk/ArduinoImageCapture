import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Created by indrek on 4.05.2016.
 */
public class ImageCapture {

  enum PixelFormat {
    PIXEL_RGB565(2),
    PIXEL_RGB565_WITH_PARITY_CHECK(2),
    PIXEL_GRAYSCALE(1);

    private int byteCount;
    PixelFormat(int byteCount) {
      this.byteCount = byteCount;
    }
    public int getByteCount() {
      return byteCount;
    }
  }

  private static final int MAX_W = 640;
  private static final int MAX_H = 480;


  private static final int PIXEL_FORMAT_CODE_MASK = 0x07;
  private static final int PIXEL_FORMAT_PARITY_CHECK_BIT = 0x80;

  // Pixel byte parity check:
  // Pixel Byte H: odd number of bits under H_BYTE_PARITY_CHECK and H_BYTE_PARITY_INVERT
  // Pixel Byte L: even number of bits under L_BYTE_PARITY_CHECK and L_BYTE_PARITY_INVERT
  //                                               H:RRRRRGGG
  private static final int H_BYTE_PARITY_CHECK =  0b00100000;
  private static final int H_BYTE_PARITY_INVERT = 0b00001000;
  //                                               L:GGGBBBBB
  private static final int L_BYTE_PARITY_CHECK =  0b00001000;
  private static final int L_BYTE_PARITY_INVERT = 0b00100000;


  private static final byte START_COMMAND = (byte) 0x00;
  private static final byte COMMAND_NEW_FRAME = (byte) 0x01;
  private static final byte COMMAND_END_OF_LINE = (byte) 0x02;
  private static final byte COMMAND_DEBUG_DATA = (byte) 0x03;

  private static final int COMMAND_NEW_FRAME_LENGTH = 5;

  int activeCommand = -1;
  int commandByteCount = 0;
  private ByteArrayOutputStream commandBytes = new ByteArrayOutputStream();
  private ByteArrayOutputStream pixelBytes = new ByteArrayOutputStream();

  private ImageFrame imageFrame = new ImageFrame(0, 0);
  private PixelFormat pixelFormat = PixelFormat.PIXEL_RGB565;



  public interface ImageCaptured {
    void imageCaptured(ImageFrame imageFrame, Integer lineNumber);
  }

  public interface DebugData {
    void debugDataReceived(String text);
  }

  private ImageCaptured imageCapturedCallback;
  private DebugData debugDataCallback;


  public ImageCapture(ImageCaptured callback, DebugData debugCallback) {
    imageCapturedCallback = callback;
    debugDataCallback = debugCallback;
  }



  public void addReceivedBytes(byte [] receivedBytes) {
    for (byte receivedByte : receivedBytes) {
      addReceivedByte(receivedByte);
    }
  }

  public void addReceivedByte(byte receivedByte) {
    if (activeCommand < 0) {
      if (receivedByte == START_COMMAND) {
        activeCommand = START_COMMAND;
      } else {
        processPixelByte(receivedByte);
      }
    } else {
      processCommandByte(receivedByte);
    }
  }

  private void processCommandByte(byte receivedByte) {
    if (activeCommand == START_COMMAND) {
      initCommand(receivedByte);
    } else {
      commandBytes.write(receivedByte);
    }

    if (commandBytes.size() >= commandByteCount) {
      if (activeCommand == COMMAND_NEW_FRAME) {
        startNewFrame(commandBytes.toByteArray());
        activeCommand =-1;
      } else if (activeCommand == COMMAND_END_OF_LINE) {
        endOfLine();
        activeCommand =-1;
      } else if (activeCommand == COMMAND_DEBUG_DATA) {
        if (processDebugData(commandBytes.toByteArray())) {
          activeCommand =-1;
        }
      } else {
        System.out.println("Unknown command code '" + activeCommand + "'");
        activeCommand =-1;
      }
    }
  }

  private void initCommand(byte command) {
    activeCommand = command;
    commandBytes.reset();
    pixelBytes.reset();
    if (command == COMMAND_NEW_FRAME) {
      commandByteCount = COMMAND_NEW_FRAME_LENGTH;
    } else if (command == COMMAND_DEBUG_DATA) {
      commandByteCount = 1;
    } else {
      commandByteCount = 0;
    }
  }

  private void startNewFrame(byte [] frameDataBytes) {
    ByteBuffer frameData = ByteBuffer.wrap(frameDataBytes);
    frameData.order(ByteOrder.BIG_ENDIAN); // or LITTLE_ENDIAN
    int w = parseFrameDimension(frameData.getShort(), 1, MAX_W);
    int h = parseFrameDimension(frameData.getShort(), 1, MAX_H);
    imageFrame = new ImageFrame(w, h);
    pixelFormat = getPixelFormat(frameData.get());
  }

  private int parseFrameDimension(int d, int min, int max) {
    return d > max ? max : (d < min ? min : d);
  }


  private PixelFormat getPixelFormat(int code) {
    switch (code & PIXEL_FORMAT_CODE_MASK) {
      default:
        System.out.println("Unknown pixel format code '" + code + "'");
      case 1:
        if ((code & PIXEL_FORMAT_PARITY_CHECK_BIT) > 0) {
          return PixelFormat.PIXEL_RGB565_WITH_PARITY_CHECK;
        } else {
          return PixelFormat.PIXEL_RGB565;
        }
      case 2:
        return PixelFormat.PIXEL_GRAYSCALE;
    }
  }


  private void endOfLine() {
    imageCapturedCallback.imageCaptured(imageFrame, imageFrame.getCurrentLineIndex());
    imageFrame.newLine();
  }


  private void processPixelByte(byte receivedByte) {
    pixelBytes.write(receivedByte);
    if (pixelBytes.size() >= pixelFormat.getByteCount()) {
      imageFrame.addPixel(readAvailablePixel());
    }
  }



  public Pixel readAvailablePixel() {
    switch (pixelFormat) {
      default:
      case PIXEL_RGB565: {
        byte [] pixelDataBytes = pixelBytes.toByteArray();
        pixelBytes.reset();
        return parse2BytePixel(pixelDataBytes);
      }
      case PIXEL_RGB565_WITH_PARITY_CHECK: {
        return readAvailablePixelWithParityCheck();
      }
      case PIXEL_GRAYSCALE: {
        int rawPixelData = pixelBytes.toByteArray()[0] & 0xFF;
        pixelBytes.reset();
        int r = rawPixelData;
        int g = rawPixelData;
        int b = rawPixelData;
        return new Pixel(r, g, b);
      }
    }
  }


  private Pixel parse2BytePixel(byte [] pixelDataBytes) {
    int rawPixelData = get2ByteInteger_H_L(pixelDataBytes);
    // rrrr rggg gggb bbbb
    int r = (rawPixelData >> 8) & 0xF8;
    int g = (rawPixelData >> 3) & 0xFC;
    int b = (rawPixelData << 3) & 0xF8;
    return new Pixel(r, g, b);
  }


  private Pixel readAvailablePixelWithParityCheck() {
    byte [] pixelDataBytes = pixelBytes.toByteArray();
    boolean isFirstByteHigh = isParityCheckHighByte(pixelDataBytes[0]);
    boolean isSecondByteLow = isParityCheckLowByte(pixelDataBytes[1]);

    if (isFirstByteHigh && isSecondByteLow) {
      pixelBytes.reset();
      return parse2BytePixel(pixelDataBytes);

    } else if (!isFirstByteHigh) {
      byte [] fixedPixedDataBytes = new byte[2];
      fixedPixedDataBytes[0] = 0; // RRRRRGGG
      fixedPixedDataBytes[1] = pixelDataBytes[0]; // GGGBBBBB
      pixelBytes.reset();
      pixelBytes.write(pixelDataBytes[1]);
      Pixel fixedPixel = parse2BytePixel(fixedPixedDataBytes);
      // Only blue is valid if only second byte is valid
      fixedPixel.invalidateR();
      fixedPixel.invalidateG();
      return fixedPixel;

    } else {
      byte [] fixedPixedDataBytes = new byte[2];
      fixedPixedDataBytes[0] = pixelDataBytes[0]; // RRRRRGGG
      fixedPixedDataBytes[1] = 0; // GGGBBBBB
      pixelBytes.reset();
      pixelBytes.write(pixelDataBytes[1]);
      Pixel fixedPixel = parse2BytePixel(fixedPixedDataBytes);
      // Only red is valid if only first byte is valid
      fixedPixel.invalidateG();
      fixedPixel.invalidateB();
      return fixedPixel;
    }
  }

  private boolean isParityCheckHighByte(byte pixelByte) {
    // RRRRRGGG
    // Pixel Byte H: odd number of bits under H_BYTE_PARITY_CHECK and H_BYTE_PARITY_INVERT
    return ((pixelByte & H_BYTE_PARITY_CHECK) > 0) != ((pixelByte & H_BYTE_PARITY_INVERT) > 0);
  }

  private boolean isParityCheckLowByte(byte pixelByte) {
    // GGGBBBBB
    // Pixel Byte L: even number of bits under L_BYTE_PARITY_CHECK and L_BYTE_PARITY_INVERT
    return ((pixelByte & L_BYTE_PARITY_CHECK) > 0) == ((pixelByte & L_BYTE_PARITY_INVERT) > 0);
  }

  private int get2ByteInteger_H_L(byte [] data) {
    if (data.length > 1) {
      return ((data[0] & 0xFF) << 8) + (data[1] & 0xFF);
    } else {
      return 0;
    }
  }

  private int get2ByteInteger_L_H(byte [] data) {
    if (data.length > 1) {
      return ((data[1] & 0xFF) << 8) + (data[0] & 0xFF);
    } else {
      return 0;
    }
  }


  private boolean processDebugData(byte [] frameDataBytes) {
    if (commandByteCount == 1) {
      int messageLength = frameDataBytes[0];
      if (messageLength == 0)  {
        return true;
      } else {
        commandByteCount = messageLength + 1;
        return false;
      }
    } else {
      String debugText = new String(frameDataBytes, 1, frameDataBytes.length - 1, StandardCharsets.UTF_8);
      debugDataCallback.debugDataReceived(debugText);
      return true;
    }
  }


}
