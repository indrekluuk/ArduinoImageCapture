package capture;


public class CommandNewFrameAction {


  private ImageCapture imageCapture;
  private byte[] commandData;
  private int commandDataLength;


  public CommandNewFrameAction(ImageCapture imageCapture, byte[] commandData, int commandDataLength) {
    this.imageCapture = imageCapture;
    this.commandData = commandData;
    this.commandDataLength = commandDataLength;
  }


  public void process() {
    if (commandDataLength != 4) {
      imageCapture.printDebugData("Invalid command length form new frame " + commandDataLength + ", should be " + 4);
    }

    int commandFrameW = (commandData[1] & 0xFF) | ((commandData[3] & 0x03) << 8);
    int commandFrameH = (commandData[2] & 0xFF) | ((commandData[3] & 0x0C) << 6);
    int commandPixelFormat = (commandData[3] & 0xF0) >> 4;

    int w = parseFrameDimension(commandFrameW, 1, ImageCapture.MAX_W);
    int h = parseFrameDimension(commandFrameH, 1, ImageCapture.MAX_H);
    PixelFormat pixelFormat = getPixelFormat(commandPixelFormat);

    imageCapture.initNewFrame(w, h, pixelFormat);
  }

  private int parseFrameDimension(int d, int min, int max) {
    return d > max ? max : (d < min ? min : d);
  }


  private PixelFormat getPixelFormat(int code) {
    switch (code) {
      default:
        imageCapture.printDebugData("Unknown pixel format code '" + code + "'");
      case 1:
        return PixelFormat.PIXEL_RGB565_WITH_PARITY_CHECK;
        // return PixelFormat.PIXEL_RGB565;
      case 2:
        return PixelFormat.PIXEL_GRAYSCALE_WITH_PARITY_CHECK;
        // return PixelFormat.PIXEL_GRAYSCALE
    }
  }


}
