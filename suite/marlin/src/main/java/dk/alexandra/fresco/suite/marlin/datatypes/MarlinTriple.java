package dk.alexandra.fresco.suite.marlin.datatypes;

public class MarlinTriple<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> {

  private final MarlinSInt<H, L, T> left;
  private final MarlinSInt<H, L, T> right;
  private final MarlinSInt<H, L, T> product;

  public MarlinTriple(MarlinSInt<H, L, T> left, MarlinSInt<H, L, T> right,
      MarlinSInt<H, L, T> product) {
    this.left = left;
    this.right = right;
    this.product = product;
  }

  public MarlinSInt<H, L, T> getLeft() {
    return left;
  }

  public MarlinSInt<H, L, T> getRight() {
    return right;
  }

  public MarlinSInt<H, L, T> getProduct() {
    return product;
  }

}
