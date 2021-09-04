package capture;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by indrek on 6.05.2016.
 */
public class Pixel {

  private int r = -1;
  private int g = -1;
  private int b = -1;

  public Pixel() {
  }

  public Pixel(int r, int g, int b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  public void invalidateR() {
    this.r = -1;
  }

  public void invalidateG() {
    this.g = -1;
  }

  public void invalidateB() {
    this.b = -1;
  }

  public Color getColor() {
    return new Color(Math.max(r, 0), Math.max(g, 0), Math.max(b, 0));
  }

  public boolean hasInvalidColors() {
    return r < 0 || g < 0 || b < 0;
  }

  public void fixColors(Collection<Pixel> surroundingPixels) {
    Collection<Integer> surroundingR = new ArrayList<>();
    Collection<Integer> surroundingG = new ArrayList<>();
    Collection<Integer> surroundingB = new ArrayList<>();
    for (Pixel surroundingPixel : surroundingPixels) {
      surroundingR.add(surroundingPixel.r);
      surroundingG.add(surroundingPixel.g);
      surroundingB.add(surroundingPixel.b);
    }
    r = averageColor(surroundingR);
    g = averageColor(surroundingG);
    b = averageColor(surroundingB);
  }

  private int averageColor(Collection<Integer> surroundingColors) {
    if (surroundingColors.size() == 0) {
      return 0;
    }
    Integer total = 0;
    for (Integer color : surroundingColors) {
      total += color;
    }
    return total / surroundingColors.size();
  }


}


