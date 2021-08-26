
import capture.ImageCapture;
import capture.ImageFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by indrek on 1.05.2016.
 */
public class MainWindow {

  private static Integer MAX_IMAGE_W = 640;
  private static Integer MAX_IMAGE_H = 480;

  private static final String WINDOW_TITLE = "Arduino Image Capture";
  private static final String BUTTON_NAME_LISTEN = "Listen";
  private static final String BUTTON_NAME_STOP = "Stop";
  private static final String BUTTON_NAME_SELECT_SAVE_FOLDER = "Select save folder";
  private static final String SELECT_SAVE_FOLDER_TILE = "Save images to";

  private static final String DEFAULT_IMAGE_DIRECTORY = "/img";


  private JFrame windowFrame;
  private JPanel mainPanel;
  private File selectedFolder;
  private JLabel saveCountLabel = new JLabel();
  private Integer saveCounter = 0;
  private BufferedImage imageBuffer;
  private JLabel imageContainer;
  private TextArea debugWindow;
  private JComboBox<String> comPortSelection;
  private JComboBox<Integer> baudRateSelection;

  private SerialReader serialReader;
  private ImageCapture imageCapture;



  public MainWindow(JFrame frame) {
    windowFrame = frame;
    imageCapture = new ImageCapture(this::drawImage, this::debugTextReceived);
    serialReader = new SerialReader(imageCapture::addReceivedByte);

    mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(createToolbar(), BorderLayout.PAGE_START);

    JPanel contentPanel = new JPanel(new BorderLayout());
    contentPanel.add(createImagePanel(), BorderLayout.PAGE_START);
    contentPanel.add(createDebugWindow());
    mainPanel.add(new JScrollPane(contentPanel));

    mainPanel.add(createSavePanel(), BorderLayout.PAGE_END);
  }


  private JComponent createSavePanel() {
    JPanel saveBar = new JPanel ();
    saveBar.setLayout(new BoxLayout(saveBar, BoxLayout.X_AXIS));
    JLabel filePathLabel = new JLabel();

    saveBar.add(createSelectFolderButton(filePathLabel));
    saveBar.add(Box.createHorizontalStrut(10));
    saveBar.add(filePathLabel);
    saveBar.add(saveCountLabel);

    return saveBar;
  }

  private JButton createSelectFolderButton(JLabel filePathLabel) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setDialogTitle(SELECT_SAVE_FOLDER_TILE);

    JButton listenButton = new JButton(BUTTON_NAME_SELECT_SAVE_FOLDER);
    listenButton.addActionListener((event)->{
      fileChooser.setCurrentDirectory(selectedFolder == null ? getDefaultSaveDirectory() : selectedFolder);

      if (fileChooser.showOpenDialog(listenButton) == JFileChooser.APPROVE_OPTION) {
        selectedFolder = fileChooser.getSelectedFile();
        filePathLabel.setText(selectedFolder.getAbsolutePath());
      }
    });
    return listenButton;
  }

  private File getDefaultSaveDirectory() {
      String currentDir = System.getProperty("user.dir");
      File dir = new File(currentDir + DEFAULT_IMAGE_DIRECTORY);
      return dir.exists() ? dir : new File(currentDir);
  }

  private JComponent createImagePanel() {
    imageBuffer = new BufferedImage(MAX_IMAGE_W,MAX_IMAGE_H, BufferedImage.TYPE_INT_ARGB);
    imageContainer = new JLabel(new ImageIcon(imageBuffer));
    return imageContainer;
  }


  private Component createDebugWindow() {
    debugWindow = new TextArea();
    return debugWindow;
  }


  private JToolBar createToolbar() {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    createComPortOption(toolBar);
    createBaudRateOption(toolBar);
    createListeningButtons(toolBar);
    return toolBar;
  }


  private void createComPortOption(JToolBar toolBar) {
    comPortSelection = new JComboBox<>();
    serialReader.getAvailablePorts().forEach(comPortSelection::addItem);
    toolBar.add(comPortSelection);
  }


  private void createBaudRateOption(JToolBar toolBar) {
    baudRateSelection = new JComboBox<>();
    serialReader.getAvailableBaudRates().forEach(baudRateSelection::addItem);
    baudRateSelection.setSelectedItem(serialReader.getDefaultBaudRate());
    toolBar.add(baudRateSelection);
  }


  private void createListeningButtons(JToolBar toolBar) {
    JButton startListenButton = new JButton(BUTTON_NAME_LISTEN);
    JButton stopListenButton = new JButton(BUTTON_NAME_STOP);
    stopListenButton.setEnabled(false);

    startListenButton.addActionListener((event)->{
      this.startListening(startListenButton, stopListenButton, event);
    });

    stopListenButton.addActionListener((event)->{
      this.stopListening(startListenButton, stopListenButton, event);
    });

    toolBar.add(startListenButton);
    toolBar.add(stopListenButton);
  }

  private void startListening(JButton startListenButton, JButton stopListenButton, ActionEvent event) {
    try {
      String selectedComPort = (String)comPortSelection.getSelectedItem();
      Integer baudRate = (Integer)baudRateSelection.getSelectedItem();
      serialReader.startListening(selectedComPort, baudRate);
      startListenButton.setEnabled(false);
      stopListenButton.setEnabled(true);
    } catch (SerialReaderException e) {
      JOptionPane.showMessageDialog(windowFrame, e.getMessage());
    }
  }

  private void stopListening(JButton startListenButton, JButton stopListenButton, ActionEvent event) {
    try {
      serialReader.stopListening();
      startListenButton.setEnabled(true);
      stopListenButton.setEnabled(false);
    } catch (SerialReaderException e) {
      JOptionPane.showMessageDialog(windowFrame, e.getMessage());
    }
  }


  private void drawImage(ImageFrame frame, Integer lineIndex) {
    JLabel imageContainer = this.imageContainer;

    // update image in a separate thread so it would not block reading data
    new Thread(() -> {
      synchronized (imageContainer) {
        Graphics2D g = imageBuffer.createGraphics();
        int fromLine = lineIndex != null ? lineIndex : 0;
        int toLine = lineIndex != null ? lineIndex : frame.getLineCount() - 1;

        for (int y = fromLine; y <= toLine; y++) {
          for (int x = 0; x < frame.getLineLength(); x++) {
            if (x < MAX_IMAGE_W && y < MAX_IMAGE_H) {
              g.setColor(frame.getPixelColor(x, y));
              g.drawLine(x, y, x, y);
            }
          }
        }
        imageContainer.repaint();
        // wait for last line to be drawn
        if (selectedFolder != null && lineIndex == frame.getLineCount() - 1) {
          saveImageToFile(imageBuffer.getSubimage(0, 0, frame.getLineLength(), frame.getLineCount()), selectedFolder);
        }
      }
    }).start();
  }


  private void debugTextReceived(String debugText) {
    System.out.println(debugText);
    debugWindow.append(debugText + "\n");
  }


  private void saveImageToFile(BufferedImage image, File toFolder) {
    try {
      // save image to png file
      File newFile = new File(toFolder.getAbsolutePath(), getNextFileName());
      ImageIO.write(image, "png", newFile);
      saveCountLabel.setText(" (" + (++saveCounter) + ")");
    } catch (Exception e) {
      System.out.println("Saving file failed: " + e.getMessage());
    }
  }

  private String getNextFileName() {
    return (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS")).format(new Date()) + ".png";
  }


  public static void main(String[] args) {
    JFrame frame = new JFrame(WINDOW_TITLE);
    MainWindow window = new MainWindow(frame);
    frame.setContentPane(window.mainPanel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

}
