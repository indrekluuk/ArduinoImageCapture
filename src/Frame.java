import java.awt.*;

/**
 * Created by indrek on 7.05.2016.
 */
public class Frame {


  private Pixel[][] pixelMatrix;
  private int lineIndex;
  private int colIndex;


  public Frame(int w, int h) {
    pixelMatrix = new Pixel[h][w];
    lineIndex = 0;
    colIndex = 0;
  }


  public void newLine() {
    if (lineIndex < getLineCount() - 1) {
      lineIndex ++;
    }
    colIndex = 0;
  }


  public void addPixel(Pixel pixel) {
    if (lineIndex < pixelMatrix.length
        && colIndex < pixelMatrix[lineIndex].length) {
      pixelMatrix[lineIndex][colIndex] = pixel;
      colIndex++;
    }
  }


  public int getLineLength() {
    return pixelMatrix.length > 0 ? pixelMatrix[0].length : 0;
  }

  public int getLineCount() {
    return pixelMatrix.length;
  }

  public int getCurrentLineIndex() {
    return lineIndex;
  }


  public Color getPixelColor(int x, int y) {
    Pixel pixel = x < getLineLength() && y < getLineCount() ? pixelMatrix[y][x] : null;
    return pixel == null ? Color.BLACK : pixel.getColor();
  }


}
