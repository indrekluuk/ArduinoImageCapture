import java.awt.*;

/**
 * Created by indrek on 7.05.2016.
 */
public class FrameData {


  private PixelData [][] pixelMatrix;
  private int lineIndex;
  private int colIndex;


  public FrameData(int w, int h) {
    pixelMatrix = new PixelData[h][w];
    lineIndex = -1;
    colIndex = 0;
  }


  public void newLine() {
    lineIndex ++;
    colIndex = 0;
  }


  public void addPixel(PixelData pixelData) {
    if (lineIndex >=0 && lineIndex <pixelMatrix.length
        && colIndex < pixelMatrix[lineIndex].length) {
      pixelMatrix[lineIndex][colIndex] = pixelData;
      colIndex++;
    }
  }


  public int getLineLength() {
    return pixelMatrix.length > 0 ? pixelMatrix[0].length : 0;
  }

  public int getLineCount() {
    return pixelMatrix.length;
  }


  public Color getPixelColor(int x, int y) {
    PixelData pixelData = pixelMatrix[y][x];
    return pixelData == null ? Color.BLACK : pixelData.getColor();
  }


}
