package dk.alexandra.fresco.suite.marlin.datatypes;

public class MarlinTriple<T extends BigUInt<T>> {

  private final MarlinElement<T> left;
  private final MarlinElement<T> right;
  private final MarlinElement<T> product;

  public MarlinTriple(MarlinElement<T> left, MarlinElement<T> right, MarlinElement<T> product) {
    this.left = left;
    this.right = right;
    this.product = product;
  }

  public MarlinElement<T> getLeft() {
    return left;
  }

  public MarlinElement<T> getRight() {
    return right;
  }

  public MarlinElement<T> getProduct() {
    return product;
  }

}
