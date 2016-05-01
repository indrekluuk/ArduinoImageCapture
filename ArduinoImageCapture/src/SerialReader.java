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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


// VM options for DLL files
// -Djava.library.path=ArduinoImageCapture\external\rxtx-win-x64



// http://stackoverflow.com/questions/15996345/java-arduino-read-data-from-the-serial-port

public class SerialReader {


  public SerialReader() {

  }


  public List<String> getAvailablePorts() {
    List<String> ports = new ArrayList<>();
    Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
    while(portEnum.hasMoreElements()) {
      CommPortIdentifier portIdentifier = (CommPortIdentifier) portEnum.nextElement();
      ports.add(portIdentifier.getName());
    }
    return ports;
  }


  public CommPortIdentifier getPortIdentifierByName(String portName) {
    Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
    while(portEnum.hasMoreElements()) {
      CommPortIdentifier portIdentifier = (CommPortIdentifier) portEnum.nextElement();
      if (portIdentifier.getName().equals(portName)) {
        return portIdentifier;
      }
    }
    return null;
  }



}