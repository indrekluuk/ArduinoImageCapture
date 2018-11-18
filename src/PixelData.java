import java.awt.*;

/**
 * Created by indrek on 6.05.2016.
 */
public class PixelData {

  private int r;
  private int g;
  private int b;


  public PixelData(int r, int g, int b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }


  public Color getColor() {
    return new Color(r, g, b);
  }

}


