
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by indrek on 1.05.2016.
 */
public class MainWindow {

  private static Integer IMAGE_W = 640;
  private static Integer IMAGE_H = 480;


  private JFrame windowFrame;
  private JPanel mainPanel;
  private BufferedImage imageBuffer;
  private JLabel imageContainer;
  private JComboBox<String> comPortSelection;
  private JComboBox<Integer> baudRateSelection;

  private SerialReader serialReader;



  public MainWindow(JFrame frame) {
    windowFrame = frame;
    serialReader = new SerialReader(new ImageCapture());

    mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(createToolbar(), BorderLayout.PAGE_START);
    mainPanel.add(createImagePanel(), BorderLayout.CENTER);
  }




  private JComponent createImagePanel() {
    imageBuffer = new BufferedImage(IMAGE_W,IMAGE_H,BufferedImage.TYPE_INT_ARGB);
    imageContainer = new JLabel(new ImageIcon(imageBuffer));

    JPanel imagePanel = new JPanel(new GridBagLayout());
    imagePanel.setPreferredSize(new Dimension(IMAGE_W, IMAGE_H));
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
    serialReader.getAvailablePorts().forEach(option -> comPortSelection.addItem(option));
    return comPortSelection;
  }


  private JComboBox createBaudRateOption() {
    baudRateSelection = new JComboBox<>();
    serialReader.getAvailableBaudRates().forEach(option -> baudRateSelection.addItem(option));
    return baudRateSelection;
  }


  private JButton createStartListeningButton() {
    JButton listenButton = new JButton("Listen");
    listenButton.addActionListener(event -> startListening());
    return listenButton;
  }



  private void startListening() {
    try {
      String selectedComPort = (String)comPortSelection.getSelectedItem();
      Integer baudRate = (Integer)baudRateSelection.getSelectedItem();
      serialReader.startListening(selectedComPort, baudRate);
    } catch (SerialReaderException e) {
      JOptionPane.showMessageDialog(windowFrame, e.getMessage());
    }
  }




  private void drawPixel(int x, int y) {
    Graphics2D g = imageBuffer.createGraphics();
    for (x = 0; x < 640; x++) {
      for (y = 0; y < 480; y++) {
        g.setColor(getRandomColor());
        g.drawRect(x, y, 1, 1);
      }
    }
    g.dispose();
    this.imageContainer.repaint();
  }

  private Color getRandomColor() {
    double rnd = Math.random();
    if (rnd < 0.3) {
      return Color.BLACK;
    } else if (rnd < 0.5) {
      return Color.WHITE;
    } else if (rnd < 0.7){
      return Color.BLUE;
    } else {
      return Color.YELLOW;
    }
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
