import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by indrek on 4.05.2016.
 */
public class ImageCapture {

  enum PixelFormat {
    PIXEL_RGB565(2),
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

  private static final byte START_COMMAND = (byte) 0x00;
  private static final byte COMMAND_NEW_FRAME = (byte) 0x01;
  private static final byte COMMAND_END_OF_LINE = (byte) 0x02;

  private static final int COMMAND_NEW_FRAME_LENGTH = 5;

  int activeCommand = -1;
  int commandByteCount = 0;
  private ByteArrayOutputStream commandBytes = new ByteArrayOutputStream();
  private ByteArrayOutputStream pixelBytes = new ByteArrayOutputStream();

  private Frame frame = new Frame(0, 0);
  private PixelFormat pixelFormat = PixelFormat.PIXEL_RGB565;



  public interface ImageCaptured {
    void imageCaptured(Frame frame, Integer lineNumber);
  }


  private ImageCaptured imageCapturedCallback;


  public ImageCapture(ImageCaptured callback) {
    imageCapturedCallback = callback;
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
      } else if (activeCommand == COMMAND_END_OF_LINE) {
        endOfLine();
      } else {
        System.out.println("Unknown command code '" + activeCommand + "'");
      }
      activeCommand =-1;
    }
  }

  private void initCommand(byte command) {
    activeCommand = command;
    commandBytes.reset();
    pixelBytes.reset();
    if (command == COMMAND_NEW_FRAME) {
      commandByteCount = COMMAND_NEW_FRAME_LENGTH;
    } else {
      commandByteCount = 0;
    }
  }

  private void startNewFrame(byte [] frameDataBytes) {
    ByteBuffer frameData = ByteBuffer.wrap(frameDataBytes);
    frameData.order(ByteOrder.BIG_ENDIAN); // or LITTLE_ENDIAN
    int w = parseFrameDimension(frameData.getShort(), 1, MAX_W);
    int h = parseFrameDimension(frameData.getShort(), 1, MAX_H);
    frame = new Frame(w, h);
    pixelFormat = getPixelFormat(frameData.get());
  }

  private int parseFrameDimension(int d, int min, int max) {
    return d > max ? max : (d < min ? min : d);
  }

  private void endOfLine() {
    imageCapturedCallback.imageCaptured(frame, frame.getCurrentLineIndex());
    frame.newLine();
  }


  private void processPixelByte(byte receivedByte) {
    pixelBytes.write(receivedByte);
    if (pixelBytes.size() >= pixelFormat.getByteCount()) {
      Pixel pixel = getPixel(pixelBytes.toByteArray());
      frame.addPixel(pixel);
      pixelBytes.reset();
    }
  }


  private PixelFormat getPixelFormat(int code) {
    switch (code) {
      default:
        System.out.println("Unknown pixel format code '" + code + "'");
      case 1:
        return PixelFormat.PIXEL_RGB565;
      case 2:
        return PixelFormat.PIXEL_GRAYSCALE;
    }
  }



  public Pixel getPixel(byte [] data) {
    switch (pixelFormat) {
      default:
      case PIXEL_RGB565: {
        int rawPixelData = get2ByteInteger_H_L(data);
        // rrrr rggg gggb bbbb
        int r = (rawPixelData >> 8) & 0xF8;
        int g = (rawPixelData >> 3) & 0xFC;
        int b = (rawPixelData << 3) & 0xF8;
        return new Pixel(r, g, b);
      }
      case PIXEL_GRAYSCALE: {
        int rawPixelData = data[0] & 0xFF;
        int r = rawPixelData;
        int g = rawPixelData;
        int b = rawPixelData;
        return new Pixel(r, g, b);
      }
    }
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
