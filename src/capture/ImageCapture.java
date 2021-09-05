package capture;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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


  private Command activeCommand = null;
  private ByteArrayOutputStream pixelBytes = new ByteArrayOutputStream();

  private ImageFrame imageFrame;
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
    initNewFrame(MAX_W, MAX_H, PixelFormat.PIXEL_RGB565);
  }

  public void initNewFrame(int w, int h, PixelFormat pixelFormat) {
    this.imageFrame = new ImageFrame(w, h, ()->{
      imageCapturedCallback.imageCaptured(imageFrame, imageFrame.getCurrentLineIndex());
    });
    this.pixelFormat = pixelFormat;
  }


  public void addReceivedBytes(byte [] receivedBytes) {
    for (byte receivedByte : receivedBytes) {
      addReceivedByte(receivedByte);
    }
  }

  public void addReceivedByte(byte receivedByte) {
    if (activeCommand == null) {
      if (receivedByte == START_COMMAND) {
        activeCommand = new Command(this);
        // Clear pixel buffer if command is received
        pixelBytes.reset();

      } else {
        processPixelByte(receivedByte);
      }

    } else {
      activeCommand.addByte(receivedByte);
      if (activeCommand.process()) {
        activeCommand = null;
      }
    }
  }


  public void printDebugData(String message) {
    debugDataCallback.debugDataReceived(message);
  }




  private void processPixelByte(byte receivedByte) {
    pixelBytes.write(receivedByte);
    if (pixelBytes.size() >= pixelFormat.getByteCount()) {
      imageFrame.addPixels(readAvailablePixels());
    }
  }


  public List<Pixel> readAvailablePixels() {
    switch (pixelFormat) {
      default:
      case PIXEL_RGB565: {
        byte [] pixelDataBytes = pixelBytes.toByteArray();
        pixelBytes.reset();
        return Arrays.asList(parse2ByteRgbPixel(pixelDataBytes));
      }
      case PIXEL_RGB565_WITH_PARITY_CHECK: {
        return Arrays.asList(readAvailableRgbPixelWithParityCheck());
      }
      case PIXEL_GRAYSCALE: {
        int rawPixelData = pixelBytes.toByteArray()[0] & 0xFF;
        pixelBytes.reset();
        return Arrays.asList(createGrayscalePixel(rawPixelData));
      }
      case PIXEL_GRAYSCALE_WITH_PARITY_CHECK: {
        return readAvailableGrayscalePixelWithParityCheck();
      }
    }
  }


  private Pixel parse2ByteRgbPixel(byte [] pixelDataBytes) {
    int rawPixelData = get2ByteInteger_H_L(pixelDataBytes);
    // rrrr rggg gggb bbbb
    int r = (rawPixelData >> 8) & 0xF8;
    int g = (rawPixelData >> 3) & 0xFC;
    int b = (rawPixelData << 3) & 0xF8;
    return new Pixel(r, g, b);
  }


  private Pixel readAvailableRgbPixelWithParityCheck() {
    byte [] pixelDataBytes = pixelBytes.toByteArray();
    boolean isFirstByteHigh = isParityCheckRgbHighByte(pixelDataBytes[0]);
    boolean isSecondByteLow = isParityCheckRgbLowByte(pixelDataBytes[1]);

    if (isFirstByteHigh && isSecondByteLow) {
      pixelBytes.reset();
      return parse2ByteRgbPixel(pixelDataBytes);

    } else if (!isFirstByteHigh) {
      byte [] fixedPixedDataBytes = new byte[2];
      fixedPixedDataBytes[0] = 0; // RRRRRGGG
      fixedPixedDataBytes[1] = pixelDataBytes[0]; // GGGBBBBB
      pixelBytes.reset();
      pixelBytes.write(pixelDataBytes[1]);
      Pixel fixedPixel = parse2ByteRgbPixel(fixedPixedDataBytes);
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
      Pixel fixedPixel = parse2ByteRgbPixel(fixedPixedDataBytes);
      // Only red is valid if only first byte is valid
      fixedPixel.invalidateG();
      fixedPixel.invalidateB();
      return fixedPixel;
    }
  }

  private boolean isParityCheckRgbHighByte(byte pixelByte) {
    // RRRRRGGG
    // Pixel Byte H: odd number of bits under H_BYTE_PARITY_CHECK and H_BYTE_PARITY_INVERT
    return ((pixelByte & H_BYTE_PARITY_CHECK) > 0) != ((pixelByte & H_BYTE_PARITY_INVERT) > 0);
  }

  private boolean isParityCheckRgbLowByte(byte pixelByte) {
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

  private List<Pixel> readAvailableGrayscalePixelWithParityCheck() {
    List<Pixel> pixels = new ArrayList<>();
    int rawPixelData1 = pixelBytes.toByteArray()[0] & 0xFF;
    int rawPixelData2 = pixelBytes.toByteArray()[1] & 0xFF;
    pixelBytes.reset();

    if (isFirstGrayscaleParityFirst(rawPixelData1)) {
      pixels.add(createGrayscalePixel(rawPixelData1));
    } else {
      pixels.add(createInvalidGrayscalePixel());
      pixels.add(createGrayscalePixel(rawPixelData1));
      pixelBytes.write(rawPixelData2);
      return pixels;
    }

    if (!isFirstGrayscaleParityFirst(rawPixelData2)) {
      pixels.add(createGrayscalePixel(rawPixelData2));
      return pixels;
    } else {
      pixels.add(createInvalidGrayscalePixel());
      pixelBytes.write(rawPixelData2);
      return pixels;
    }
  }

  private boolean isFirstGrayscaleParityFirst(int rawPixelData) {
    return (rawPixelData & 1) == 0;
  }

  private Pixel createGrayscalePixel(int c) {
    return new Pixel(c, c, c);
  }

  private Pixel createInvalidGrayscalePixel() {
    return new Pixel();
  }



}
