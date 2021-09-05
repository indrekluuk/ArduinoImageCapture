package capture;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CommandDebugDataAction {

  private ImageCapture imageCapture;
  private byte[] commandData;
  private int commandDataLength;

  public CommandDebugDataAction(ImageCapture imageCapture, byte[] commandData, int commandDataLength) {
    this.imageCapture = imageCapture;
    this.commandData = commandData;
    this.commandDataLength = commandDataLength;
  }

  public void process() {
    if (commandDataLength > 0) {
      String debugText = new String(Arrays.copyOfRange(commandData, 1, commandDataLength), StandardCharsets.UTF_8);
      imageCapture.printDebugData(debugText);
    }
  }

}
