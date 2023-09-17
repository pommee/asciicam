import static java.awt.Font.PLAIN;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static org.bytedeco.javacv.FrameGrabber.createDefault;
import static org.bytedeco.javacv.Java2DFrameUtils.toBufferedImage;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;
import org.bytedeco.opencv.opencv_core.Mat;

public class Main {

  private static final String ASCII_CHARS_BRIGHTNESS = " .,:ilwW";
  private static final String ASCII_CHARS_BRIGHTNESS_REVERSED = "Wwli:,. ";

  private static final int FRAME_WIDTH = 1400;
  private static final int FRAME_HEIGHT = 880;

  private static final int ASCII_WIDTH = 250;
  private static final int ASCII_HEIGHT = 60;

  private static final int FONT_SIZE = 10;

  private static BufferedImage image;
  private static Frame frame;

  public static void main(String[] args) throws Exception {

    FrameGrabber grabber = createDefault(0);
    grabber.start();

    var jFrame = initJFrame();

    JTextArea textArea = new JTextArea();
    textArea.setBackground(Color.BLACK);
    textArea.setFont(new Font("Monospaced", PLAIN, FONT_SIZE));
    jFrame.add(textArea);

    double scaleX = grabber.getImageWidth() / (double) ASCII_WIDTH;
    double scaleY = grabber.getImageHeight() / (double) ASCII_HEIGHT;

    while ((frame = grabber.grab()) != null) {
      try (ToMat converter = new ToMat()) {
        Mat mat = converter.convert(frame);
        image = toBufferedImage(mat);
      }

      var asciiArt = new StringBuilder();

      for (int y = 0; y < ASCII_HEIGHT; y++) {
        for (int x = 0; x < ASCII_WIDTH; x++) {
          int pixelX = (int) (x * scaleX);
          int pixelY = (int) (y * scaleY);
          int rgb = image.getRGB(pixelX, pixelY);

          int red = (rgb >> 16) & 0xFF;
          int green = (rgb >> 8) & 0xFF;
          int blue = rgb & 0xFF;

          // Calculate grayscale using weighted average
          int gray = (int) (0.2989 * red + 0.5870 * green + 0.1140 * blue);
          int index = (int) (gray / 255.0 * (ASCII_CHARS_BRIGHTNESS_REVERSED.length() - 1));
          char asciiChar = ASCII_CHARS_BRIGHTNESS_REVERSED.charAt(index);

          asciiArt.append(asciiChar);
        }
        asciiArt.append("\n");
      }
      textArea.setText(asciiArt.toString());
    }
  }

  private static JFrame initJFrame() {
    JFrame jFrame = new JFrame("Ascii Webcam");
    jFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
    jFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
    jFrame.setVisible(true);
    jFrame.requestFocus();

    return jFrame;
  }

}
