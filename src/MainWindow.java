
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

/**
 * Created by indrek on 1.05.2016.
 */
public class MainWindow {

  private static Integer MAX_IMAGE_W = 640;
  private static Integer MAX_IMAGE_H = 480;


  private JFrame windowFrame;
  private JPanel mainPanel;
  private BufferedImage imageBuffer;
  private JLabel imageContainer;
  private JComboBox<String> comPortSelection;
  private JComboBox<Integer> baudRateSelection;

  private SerialReader serialReader;
  private ImageCapture imageCapture;



  public MainWindow(JFrame frame) {
    windowFrame = frame;
    imageCapture = new ImageCapture(this::drawImage);
    serialReader = new SerialReader(imageCapture::addReceivedByte);

    mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(createToolbar(), BorderLayout.PAGE_START);
    mainPanel.add(createImagePanel(), BorderLayout.CENTER);
  }




  private JComponent createImagePanel() {
    imageBuffer = new BufferedImage(MAX_IMAGE_W,MAX_IMAGE_H,BufferedImage.TYPE_INT_ARGB);
    imageContainer = new JLabel(new ImageIcon(imageBuffer));

    JPanel imagePanel = new JPanel(new GridBagLayout());
    imagePanel.setPreferredSize(new Dimension(MAX_IMAGE_W, MAX_IMAGE_H));
    imagePanel.add(imageContainer);

    return new JScrollPane(imagePanel);
  }


  private JToolBar createToolbar() {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(createComPortOption());
    toolBar.add(createBaudRateOption());
    toolBar.add(createStartListeningButton());
    return toolBar;
  }


  private JComboBox createComPortOption() {
    comPortSelection = new JComboBox<>();
    serialReader.getAvailablePorts().forEach(comPortSelection::addItem);
    return comPortSelection;
  }


  private JComboBox createBaudRateOption() {
    baudRateSelection = new JComboBox<>();
    serialReader.getAvailableBaudRates().forEach(baudRateSelection::addItem);
    return baudRateSelection;
  }


  private JButton createStartListeningButton() {
    JButton listenButton = new JButton("Listen");
    listenButton.addActionListener((event)->{
      this.startListening(listenButton, event);
    });
    return listenButton;
  }


  private void startListening(JButton listenButton, ActionEvent event) {
    try {
      String selectedComPort = (String)comPortSelection.getSelectedItem();
      Integer baudRate = (Integer)baudRateSelection.getSelectedItem();
      serialReader.startListening(selectedComPort, baudRate);
      listenButton.setEnabled(false);
    } catch (SerialReaderException e) {
      JOptionPane.showMessageDialog(windowFrame, e.getMessage());
    }
  }



  private void drawImage(FrameData frameData) {
    Graphics2D g = imageBuffer.createGraphics();

    for (int y = 0; y < frameData.getLineCount(); y++) {
      for (int x = 0; x < frameData.getLineLength(); x++) {
        if (x < MAX_IMAGE_W && y < MAX_IMAGE_H) {
          g.setColor(frameData.getPixelColor(x, y));
          g.drawRect(x, y, 1, 1);
        }
      }
    }
    g.dispose();
    this.imageContainer.repaint();
  }



  public static void main(String[] args) {
    JFrame frame = new JFrame("Arduino Image Capture");
    MainWindow window = new MainWindow(frame);
    frame.setContentPane(window.mainPanel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

}
