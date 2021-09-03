/**
 * Created by indrek on 1.05.2016.
 */

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.InputStream;
import java.io.OutputStream;

import java.util.*;



public class SerialReader implements SerialPortDataListener {

  private SerialPort serialPort;
  private InputStream serialInput;
  private OutputStream serialOutput;

  private static final int TIME_OUT = 2000;


  private List<Integer> baudRateList = Arrays.asList(
      2000000, //may be unreliable
      1000000,
      500000,
      250000,
      230400,
      115200/*,
      57600,
      38400,
      19200,
      9600*/);

  public interface SerialDataReceived {
    void serialDataReceived(byte receivedByte);
  }

  private SerialDataReceived serialReceivedCallback;


  public SerialReader(SerialDataReceived callback) {
    serialReceivedCallback = callback;
  }




  public void startListening(String portName, Integer baudRate) {
    SerialPort serialPort = getSerialPorts().get(portName);
    if (serialPort == null) {
      throw new SerialReaderException("'" + portName + "' not found");
    } else {
      openPort(serialPort, baudRate);
    }
  }



  private synchronized void openPort(
      SerialPort openSerialPort,
      Integer baudRate
  ) {
    try {
      stopListening();

      serialPort = openSerialPort;
      serialPort.openPort();

      serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, TIME_OUT, TIME_OUT);
      serialPort.setComPortParameters(
              baudRate,
              8,
              SerialPort.ONE_STOP_BIT,
              SerialPort.NO_PARITY);

      serialInput = serialPort.getInputStream();
      serialOutput = serialPort.getOutputStream();

      serialPort.addDataListener(this);
    } catch (Exception e) {
      throw new SerialReaderException("Connect failed " + e.getMessage());
    }
  }


  @Override
  public int getListeningEvents() {
    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
  }

  @Override
  public synchronized void serialEvent(SerialPortEvent oEvent) {
    if (oEvent.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
      try {
        while(serialInput.available() > 0) {
          int b = serialInput.read();
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
      serialPort.removeDataListener();
      serialPort.closePort();
      serialPort = null;
    }
  }


  public List<String> getAvailablePorts() {
    List<String> ports = new ArrayList<>(getSerialPorts().keySet());
    Collections.reverse(ports);
    return ports;
  }

  public List<Integer> getAvailableBaudRates() {
    return baudRateList;
  }

  public Integer getDefaultBaudRate() {
    return baudRateList.get(2);
  }

  private Map<String, SerialPort> getSerialPorts() {
    Map<String, SerialPort> portIdentifierMap = new LinkedHashMap<>();
    SerialPort serialPorts [] = SerialPort.getCommPorts();
    for(SerialPort serialPort : serialPorts) {
      portIdentifierMap.put(serialPort.getSystemPortName(), serialPort);
    }
    return portIdentifierMap;
  }







}