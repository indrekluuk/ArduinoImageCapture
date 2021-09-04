package capture;

public enum PixelFormat {
  PIXEL_RGB565(2),
  PIXEL_RGB565_WITH_PARITY_CHECK(2),
  PIXEL_GRAYSCALE(1),
  PIXEL_GRAYSCALE_WITH_PARITY_CHECK(2);

  private int byteCount;
  PixelFormat(int byteCount) {
    this.byteCount = byteCount;
  }
  public int getByteCount() {
    return byteCount;
  }
}
