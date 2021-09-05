package capture.commands;

import capture.ImageCapture;

public class StartCommand extends AbstractCommand {

  private static final int SUPPORTED_COMMAND_VERSION = 0x10;
  private static final int COMMAND_MASK = 0b00001111;

  private static final byte COMMAND_NEW_FRAME = (byte) 0x01;
  private static final byte COMMAND_DEBUG_DATA = (byte) 0x03;


  private ImageCapture imageCapture;


  public StartCommand(ImageCapture imageCapture) {
    super(1);
    this.imageCapture = imageCapture;
  }


  @Override
  public AbstractCommand commandReceived() {
    int commandByte = commandBytes.toByteArray()[0];
    int commandCode = commandByte & COMMAND_MASK;
    int commandVersion = commandByte & ~COMMAND_MASK;

    if (commandVersion != SUPPORTED_COMMAND_VERSION) {
      imageCapture.printDebugData("Received command version 0x" + Integer.toHexString(commandVersion) + ". Supported command version 0x" + Integer.toHexString(SUPPORTED_COMMAND_VERSION) + ". Please update!");
      return null;
    }

    switch (commandCode) {
      case COMMAND_NEW_FRAME:
        return new CommandNewFrame(imageCapture).process();
      case COMMAND_DEBUG_DATA:
        return new CommandDebugData(imageCapture).process();
      default:
        imageCapture.printDebugData("Unknown command code '" + commandCode + "'");
        return null;
    }
  }

}
