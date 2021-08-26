package capture.commands;

import capture.ImageCapture;
import capture.PixelFormat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CommandNewFrame extends AbstractCommand {

  private static final int COMMAND_FRAME_VERSION_0 = 0x00;
  private static final int COMMAND_FRAME_VERSION_1 = 0x10;

  private static final int COMMAND_FRAME_VERSION_0_FRAME_LENGTH = 5;

  private static final int PIXEL_FORMAT_CODE_MASK = 0b00000111;
  private static final int PIXEL_FORMAT_PARITY_CHECK_BIT = 0x80;



  int commandVersion;
  private ImageCapture imageCapture;


  public CommandNewFrame(int commandVersion, ImageCapture imageCapture) {
    super(COMMAND_FRAME_VERSION_0 == commandVersion ?
        COMMAND_FRAME_VERSION_0_FRAME_LENGTH :
        COMMAND_LENGTH_FROM_FIRST_BYTE);
    this.commandVersion = commandVersion;
    this.imageCapture = imageCapture;
  }


  @Override
  public AbstractCommand commandReceived() {

    switch (commandVersion) {
      case COMMAND_FRAME_VERSION_0:
        processNewFrame_version_0();
        break;
      //case COMMAND_FRAME_VERSION_1:
      //  break;
      default:
        imageCapture.printDebugData("Frame version 0x" + Integer.toHexString(commandVersion) + " not supported! Please download updated version of this program.");
        break;
    }

    return null;
  }


  private void processNewFrame_version_0() {
    ByteBuffer frameData = ByteBuffer.wrap(commandBytes.toByteArray());
    frameData.order(ByteOrder.BIG_ENDIAN); // or LITTLE_ENDIAN
    int w = parseFrameDimension(frameData.getShort(), 1, ImageCapture.MAX_W);
    int h = parseFrameDimension(frameData.getShort(), 1, ImageCapture.MAX_H);
    imageCapture.initNewFrame(w, h, getPixelFormat(frameData.get()));
  }


  private int parseFrameDimension(int d, int min, int max) {
    return d > max ? max : (d < min ? min : d);
  }


  private PixelFormat getPixelFormat(int code) {
    switch (code & PIXEL_FORMAT_CODE_MASK) {
      default:
        imageCapture.printDebugData("Unknown pixel format code '" + code + "'");
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


}
