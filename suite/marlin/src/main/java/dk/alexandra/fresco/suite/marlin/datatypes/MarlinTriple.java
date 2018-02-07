package dk.alexandra.fresco.suite.marlin.datatypes;

public class MarlinTriple<T extends BigUInt<T>> {

  private final MarlinSInt<T> left;
  private final MarlinSInt<T> right;
  private final MarlinSInt<T> product;

  public MarlinTriple(MarlinSInt<T> left, MarlinSInt<T> right, MarlinSInt<T> product) {
    this.left = left;
    this.right = right;
    this.product = product;
  }

  public MarlinSInt<T> getLeft() {
    return left;
  }

  public MarlinSInt<T> getRight() {
    return right;
  }

  public MarlinSInt<T> getProduct() {
    return product;
  }

}
