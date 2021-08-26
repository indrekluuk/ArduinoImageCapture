package capture.commands;


import java.io.ByteArrayOutputStream;

public abstract class AbstractCommand {

  protected static int COMMAND_LENGTH_FROM_FIRST_BYTE = -1;

  private int commandLength;
  protected ByteArrayOutputStream commandBytes = new ByteArrayOutputStream();

  protected AbstractCommand(int commandLength) {
    this.commandLength = commandLength;
  }


  public void addByte(byte receivedByte) {
    if (commandLength == COMMAND_LENGTH_FROM_FIRST_BYTE) {
      commandLength = receivedByte & 0xFF;
    } else {
      commandBytes.write(receivedByte);
    }
  }

  public final AbstractCommand process() {
    if (commandLength != COMMAND_LENGTH_FROM_FIRST_BYTE && commandBytes.size() >= commandLength) {
      return commandReceived();
    } else {
      return this;
    }
  }

  protected abstract AbstractCommand commandReceived();


}
