package capture.commands;

import capture.ImageCapture;

public class StartCommand extends AbstractCommand {

  private static final int COMMAND_MASK = 0b00001111;

  private static final byte COMMAND_NEW_FRAME = (byte) 0x01;
  private static final byte COMMAND_END_OF_LINE = (byte) 0x02;
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

    switch (commandCode) {
      case COMMAND_NEW_FRAME:
        return new CommandNewFrame(commandVersion, imageCapture).process();
      case COMMAND_END_OF_LINE:
        return new CommandEndOfLine(imageCapture).process();
      case COMMAND_DEBUG_DATA:
        return new CommandDebugData(imageCapture).process();
      default:
        imageCapture.printDebugData("Unknown command code '" + commandCode + "'");
        return null;
    }
  }

}
