/**
 * Created by indrek on 1.05.2016.
 */

import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.*;


// VM options for DLL files
// -Djava.library.path=lib\rxtx-2.2pre2-bins\win64



// http://stackoverflow.com/questions/15996345/java-arduino-read-data-from-the-serial-port

public class SerialReader implements SerialPortEventListener {

  private SerialPort serialPort;
  private InputStream serialInput;
  private OutputStream serialOutput;

  private static final int TIME_OUT = 2000;


  private List<Integer> baudRateList = Arrays.asList(
      2000000, //may be unreliable
      1000000,
      115200,
      57600,
      38400,
      19200,
      9600);

  public interface SerialDataReceived {
    void serialDataReceived(byte receivedByte);
  }

  private SerialDataReceived serialReceivedCallback;


  public SerialReader(SerialDataReceived callback) {
    serialReceivedCallback = callback;
  }




  public void startListening(String portName, Integer baudRate) {
    CommPortIdentifier portIdentifier = getPortIdentifiers().get(portName);
    if (portIdentifier == null) {
      throw new SerialReaderException("'" + portName + "' not found");
    } else {
      openPort(portIdentifier, baudRate);
    }
  }



  private synchronized void openPort(
      CommPortIdentifier portIdentifier,
      Integer baudRate
  ) {
    try {
      stopListening();

      serialPort = (SerialPort) portIdentifier.open(
          this.getClass().getName(),
          TIME_OUT);

      serialPort.setSerialPortParams(
          baudRate,
          SerialPort.DATABITS_8,
          SerialPort.STOPBITS_1,
          SerialPort.PARITY_NONE);

      serialInput = serialPort.getInputStream();
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
        int b;
        while((b = serialInput.read()) > -1) {
          serialReceivedCallback.serialDataReceived((byte)(b));
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
    List<String> ports = new ArrayList<>(getPortIdentifiers().keySet());
    Collections.reverse(ports);
    return ports;
  }

  public List<Integer> getAvailableBaudRates() {
    return baudRateList;
  }

  public Integer getDefaultBaudRate() {
    return baudRateList.get(1);
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