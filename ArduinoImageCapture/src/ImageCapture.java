import org.omg.CORBA.PRIVATE_MEMBER;

import java.util.List;

/**
 * Created by indrek on 4.05.2016.
 */
public class ImageCapture {

  enum PixelFormat {
    PIXEL_RGB565(2),
    PIXEL_BAYERRGB(1);

    private int byteCount;
    PixelFormat(int byteCount) {
      this.byteCount = byteCount;
    }
    public int getByteCount() {
      return byteCount;
    }
  }

  private static final int MAX_X = 640;
  private static final int MAX_Y = 480;

  private static final int MAX_BUFFER_SIZE = MAX_X * MAX_Y * 2 + 1000;
  private byte buffer[] = new byte[MAX_BUFFER_SIZE];
  private int bufferFillIndex = 0;

  private static final byte END_OF_LINE[] = new byte[] {
      (byte)0x01, (byte)0x01, (byte)0xFE, (byte)0xFE, (byte)0xFE};

  private static final byte END_OF_FRAME[] = new byte[] {
      (byte)0xFE, (byte)0xFE, (byte)0x01, (byte)0x01, (byte)0x01};



  public interface ImageCaptured {
    void imageCaptured(FrameData frameData);
  }


  private ImageCaptured imageCapturedCallback;


  public ImageCapture(ImageCaptured callback) {
    imageCapturedCallback = callback;
  }




  public void addReceivedByte(byte receivedByte) {
    buffer[bufferFillIndex] = receivedByte;
    bufferFillIndex++;
    if (bufferFillIndex >= MAX_BUFFER_SIZE) bufferFillIndex = 0;

    if (isReceived(END_OF_FRAME, bufferFillIndex)) {
      processFrameBuffer();
      bufferFillIndex = 0;
    }
  }



  private boolean isReceived(byte [] sequence, int bufferIndex) {
    if (bufferIndex < sequence.length) {
      return false;
    }
    for (int i=0; i<sequence.length; i++) {
      if (sequence[i] != buffer[bufferIndex - sequence.length + i]) {
        return false;
      }
    }
    return true;
  }



  private void processFrameBuffer() {
    int imageWidth = get2ByteInteger_H_L(bufferFillIndex - END_OF_FRAME.length - 1 - 2 - 2);
    int imageHeight = get2ByteInteger_H_L(bufferFillIndex - END_OF_FRAME.length - 1 - 2);
    PixelFormat pixelFormat = getPixelFormat(getByte(bufferFillIndex - END_OF_FRAME.length - 1));

    if (imageWidth <= MAX_X && imageHeight <= MAX_Y) {
      FrameData frameData = new FrameData(imageWidth, imageHeight);

      int lineStart = 0;
      for(int bufferIndex = 0; bufferIndex < bufferFillIndex; bufferIndex++) {
        if (isReceived(END_OF_LINE, bufferIndex)) {
          frameData.newLine();
          processLine(
              frameData,
              lineStart,
              bufferIndex - END_OF_LINE.length,
              pixelFormat);
          lineStart = bufferIndex;
        }
      }

      imageCapturedCallback.imageCaptured(frameData);
    }
  }

  private void processLine(
      FrameData frameData,
      int index,
      int lineEndIndex,
      PixelFormat pixelFormat
  ) {
    while (index < lineEndIndex) {
      PixelData pixelData = getPixel(index, pixelFormat);
      frameData.addPixel(pixelData);
      index += pixelFormat.getByteCount();
    }
  }


  private PixelFormat getPixelFormat(int code) {
    switch (code) {
      default:
      case 0: return PixelFormat.PIXEL_RGB565;
      case 1: return PixelFormat.PIXEL_BAYERRGB;
    }
  }


  private PixelData getPixel(int index, PixelFormat pixelFormat) {
    switch (pixelFormat) {
      default:
      case PIXEL_RGB565:
        int rawPixelData = get2ByteInteger_H_L(index);
        // rrrr rggg gggb bbbb
        int r = (rawPixelData >> 8) & 0xF8;
        int g = (rawPixelData >> 3) & 0xFC;
        int b = (rawPixelData << 3) & 0xF8;
        return new PixelData(r, g, b);

      case PIXEL_BAYERRGB:
        int data = getByte(index);
        return new PixelData(data, data, data);
    }
  }



  private int getByte(int bufferIndex) {
    return buffer[bufferIndex] & 0xFF;
  }

  private int get2ByteInteger_H_L(int bufferIndex) {
    return ((buffer[bufferIndex] & 0xFF) << 8) + (buffer[bufferIndex + 1] & 0xFF);
  }

  private int get2ByteInteger_L_H(int bufferIndex) {
    return ((buffer[bufferIndex + 1] & 0xFF) << 8) + (buffer[bufferIndex] & 0xFF);
  }


}
