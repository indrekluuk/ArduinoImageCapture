package capture;

import capture.commands.AbstractCommand;
import capture.commands.StartCommand;

import java.io.ByteArrayOutputStream;

/**
 * Created by indrek on 4.05.2016.
 */
public class ImageCapture {

  public static final int MAX_W = 640;
  public static final int MAX_H = 480;


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


  private AbstractCommand activeCommand = null;
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
    if (activeCommand == null) {
      if (receivedByte == START_COMMAND) {
        activeCommand = new StartCommand(this);

      } else {
        processPixelByte(receivedByte);
      }

    } else {
      activeCommand.addByte(receivedByte);
      activeCommand = activeCommand.process();
      if (activeCommand == null) {
        // Reset pixel buffer if command is done.
        pixelBytes.reset();
      }
    }
  }

  public void initNewFrame(int w, int h, PixelFormat pixelFormat) {
    this.imageFrame = new ImageFrame(w, h);
    this.pixelFormat = pixelFormat;
  }

  public void endOfLine() {
    imageCapturedCallback.imageCaptured(imageFrame, imageFrame.getCurrentLineIndex());
    imageFrame.newLine();
  }

  public void printDebugData(String message) {
    debugDataCallback.debugDataReceived(message);
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


}
