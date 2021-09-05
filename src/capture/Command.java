package capture;


import java.io.ByteArrayOutputStream;

public class Command {

  private static final int SUPPORTED_COMMAND_VERSION = 0x10;
  private static final int COMMAND_MASK = 0b00001111;

  private static final byte COMMAND_NEW_FRAME = (byte) 0x01;
  private static final byte COMMAND_DEBUG_DATA = (byte) 0x03;

  private int commandDataLength = -1;
  protected ByteArrayOutputStream commandBytes = new ByteArrayOutputStream();


  private ImageCapture imageCapture;


  public Command(ImageCapture imageCapture) {
    this.imageCapture = imageCapture;
  }

  public void addByte(byte receivedByte) {
    if (commandDataLength < 0) {
      commandDataLength = (receivedByte & 0xFF);
    } else {
      commandBytes.write(receivedByte);
    }
  }

  public boolean process() {

    // Command length not initialized yet
    if (commandDataLength < 0) {
      return false;
    }

    // Waiting for command data
    if (commandBytes.size() < (commandDataLength + 1)) {  // +1 for checksum byte
      return false;
    }

    byte[] receivedCommandData = commandBytes.toByteArray();

    if (!isChecksumValid(receivedCommandData)) {
      imageCapture.printDebugData("" +
          "Command checksum failed!\n" +
          "1. Check baud rate\n" +
          "2. Download the latest version of the Arduino code and the image capture program.");
      return true;
    }

    if (commandDataLength == 0) {
      imageCapture.printDebugData("Received empty command.");
      return true;
    }

    int commandByte = receivedCommandData[0];
    int commandCode = commandByte & COMMAND_MASK;
    int commandVersion = commandByte & ~COMMAND_MASK;

    if (commandVersion != SUPPORTED_COMMAND_VERSION) {
      imageCapture.printDebugData("" +
          "Received command version 0x" + Integer.toHexString(commandVersion) + ". Supported command version 0x" + Integer.toHexString(SUPPORTED_COMMAND_VERSION) + ".\n" +
          "Download the latest version of the Arduino code and the image capture program.");
    }

    processReceivedCommand(commandCode, receivedCommandData);
    return true;
  }

  private boolean isChecksumValid(byte[] receivedCommandData) {
    int checksum = 0;
    for (int i = 0; i < commandDataLength; i++) {
      checksum ^= receivedCommandData[i];
    }
    return checksum == receivedCommandData[commandDataLength];
  }


  private void processReceivedCommand(int commandCode, byte[] receivedCommandData) {
    switch (commandCode) {
      case COMMAND_NEW_FRAME:
        new CommandNewFrameAction(imageCapture, receivedCommandData, commandDataLength).process();
        return;
      case COMMAND_DEBUG_DATA:
        new CommandDebugDataAction(imageCapture, receivedCommandData, commandDataLength).process();
        return;
      default:
        imageCapture.printDebugData("Unknown command code '" + commandCode + "'");
        return;
    }
  }

}
