import static java.awt.BorderLayout.SOUTH;
import static org.bytedeco.javacv.FrameGrabber.createDefault;
import static org.bytedeco.javacv.Java2DFrameUtils.toBufferedImage;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;
import org.bytedeco.opencv.opencv_core.Mat;

public class Main {

  private static final String ASCII_CHARS_BRIGHTNESS = " .,:ilwW";
  private static final String ASCII_CHARS_BRIGHTNESS_REVERSED = "Wwli:,. ";

  private static final int ASCII_WIDTH = 250;
  private static final int ASCII_HEIGHT = 60;
  private static final int FONT_SIZE = 10;

  private static BufferedImage image;
  private static Frame frame;
  private static String currentAsciiChars = ASCII_CHARS_BRIGHTNESS;

  private static JTextField asciiBrightnessInput;

  public static void main(String[] args) throws Exception {
    FrameGrabber grabber = createDefault(0);
    grabber.start();

    JFrame jFrame = initJFrame();
    JTextArea textArea = createAndConfigureTextArea();
    jFrame.add(textArea);

    double scaleX = grabber.getImageWidth() / (double) ASCII_WIDTH;
    double scaleY = grabber.getImageHeight() / (double) ASCII_HEIGHT;

    configureMenu(jFrame);

    JPanel inputPanel = createAndConfigureInputPanel();
    jFrame.add(inputPanel, SOUTH);
    setListeners();

    while ((frame = grabber.grab()) != null) {
      updateImage();
      String asciiArt = generateAsciiArt(scaleX, scaleY);
      textArea.setText(asciiArt);
      jFrame.pack();
    }
  }

  private static JTextArea createAndConfigureTextArea() {
    JTextArea textArea = new JTextArea();
    textArea.setBackground(Color.BLACK);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));
    return textArea;
  }

  private static void configureMenu(JFrame jFrame) {
    JMenuBar menuBar = new JMenuBar();
    JMenu asciiCharsMenu = new JMenu("ASCII Brightness");
    JMenuItem lightBrightness = new JMenuItem("Light");
    JMenuItem darkBrightness = new JMenuItem("Dark");

    lightBrightness.addActionListener(e -> updateAsciiChars(ASCII_CHARS_BRIGHTNESS));
    darkBrightness.addActionListener(e -> updateAsciiChars(ASCII_CHARS_BRIGHTNESS_REVERSED));
    darkBrightness.addActionListener(e -> updateAsciiChars(ASCII_CHARS_BRIGHTNESS_REVERSED));

    asciiCharsMenu.add(lightBrightness);
    asciiCharsMenu.add(darkBrightness);
    menuBar.add(asciiCharsMenu);
    jFrame.setJMenuBar(menuBar);
  }

  private static JPanel createAndConfigureInputPanel() {
    JPanel inputPanel = new JPanel();
    asciiBrightnessInput = new JTextField(10);
    JLabel instructionLabel = new JLabel("Enter custom ASCII characters:");
    asciiBrightnessInput.setText(currentAsciiChars);
    inputPanel.add(instructionLabel);
    inputPanel.add(asciiBrightnessInput);
    return inputPanel;
  }

  private static void updateImage() {
    try (ToMat converter = new ToMat()) {
      Mat mat = converter.convert(frame);
      image = toBufferedImage(mat);
    }
  }

  private static String generateAsciiArt(double scaleX, double scaleY) {
    StringBuilder asciiArt = new StringBuilder();

    for (int y = 0; y < ASCII_HEIGHT; y++) {
      for (int x = 0; x < ASCII_WIDTH; x++) {
        int pixelX = (int) (x * scaleX);
        int pixelY = (int) (y * scaleY);
        int rgb = image.getRGB(pixelX, pixelY);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        int gray = (int) (0.2989 * red + 0.5870 * green + 0.1140 * blue);
        int index = (int) (gray / 255.0 * (currentAsciiChars.length() - 1));
        char asciiChar = currentAsciiChars.charAt(index);
        asciiArt.append(asciiChar);
      }
      asciiArt.append("\n");
    }
    return asciiArt.toString();
  }

  private static void updateAsciiChars(String newChars) {
    currentAsciiChars = newChars;

    try {
      asciiBrightnessInput.setText(newChars);
    } catch (IllegalStateException ignored) {
    }
  }

  private static void setListeners() {
    asciiBrightnessInput.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        updateAsciiChars(asciiBrightnessInput.getText());
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateAsciiChars(asciiBrightnessInput.getText());
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
      }
    });
  }

  private static JFrame initJFrame() {
    JFrame jFrame = new JFrame("Ascii Webcam");
    jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jFrame.setVisible(true);
    jFrame.requestFocus();
    return jFrame;
  }

}
