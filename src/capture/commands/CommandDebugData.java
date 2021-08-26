package capture.commands;


import capture.ImageCapture;
import java.nio.charset.StandardCharsets;

public class CommandDebugData extends AbstractCommand {

  private ImageCapture imageCapture;


  public CommandDebugData(ImageCapture imageCapture) {
    super(COMMAND_LENGTH_FROM_FIRST_BYTE);
    this.imageCapture = imageCapture;
  }


  @Override
  public AbstractCommand commandReceived() {
    String debugText = new String(commandBytes.toByteArray(), StandardCharsets.UTF_8);
    imageCapture.printDebugData(debugText);
    return null;
  }

}
