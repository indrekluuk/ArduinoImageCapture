/**
 * Created by indrek on 1.05.2016.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.*;


// VM options for DLL files
// -Djava.library.path=ArduinoImageCapture\external\rxtx-win-x64



// http://stackoverflow.com/questions/15996345/java-arduino-read-data-from-the-serial-port

public class SerialReader implements SerialPortEventListener {

  private SerialPort serialPort;
  private ImageCapture imageCapture;
  private BufferedReader serialInput;
  private OutputStream serialOutput;

  private static final int TIME_OUT = 2000;
  private static final int DATA_RATE = 9600;

  public SerialReader(ImageCapture imageCapture) {
    this.imageCapture = imageCapture;
  }




  public void startListening(String portName) {
    CommPortIdentifier portIdentifier = getPortIdentifiers().get(portName);
    if (portIdentifier == null) {
      throw new SerialReaderException("'" + portName + "' not found");
    } else {
      openPort(portIdentifier);
    }
  }



  private synchronized void openPort(CommPortIdentifier portIdentifier) {
    try {
      stopListening();

      serialPort = (SerialPort) portIdentifier.open(
          this.getClass().getName(),
          TIME_OUT);

      serialPort.setSerialPortParams(
          DATA_RATE,
          SerialPort.DATABITS_8,
          SerialPort.STOPBITS_1,
          SerialPort.PARITY_NONE);

      // open the streams
      serialInput = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
      serialOutput = serialPort.getOutputStream();

      serialPort.addEventListener(this);
      serialPort.notifyOnDataAvailable(true);
    } catch (Exception e) {
      throw new SerialReaderException("Connect failed " + e.getMessage());
    }
  }



  public synchronized void serialEvent(SerialPortEvent oEvent) {
    if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
      try {
        while(serialInput.ready()) {
          int b = serialInput.read();
          System.out.println(b);
        }
      } catch (Exception e) {
        System.err.println(e.toString());
      }
    } else {
      System.out.println("Received event " + oEvent.getEventType());
    }
  }



  public synchronized void stopListening() {
    if (serialPort != null) {
      serialPort.removeEventListener();
      serialPort.close();
      serialPort = null;
    }
  }


  public List<String> getAvailablePorts() {
    return new ArrayList<>(getPortIdentifiers().keySet());
  }


  private Map<String, CommPortIdentifier> getPortIdentifiers() {
    Map<String, CommPortIdentifier> portIdentifierMap = new LinkedHashMap<>();
    Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
    while(portEnum.hasMoreElements()) {
      CommPortIdentifier portIdentifier = (CommPortIdentifier) portEnum.nextElement();
      portIdentifierMap.put(portIdentifier.getName(), portIdentifier);
    }
    return portIdentifierMap;
  }







}