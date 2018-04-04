package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class Spdz2kTriple<PlainT extends CompUInt<?, ?, PlainT>> {

  private final Spdz2kSInt<PlainT> left;
  private final Spdz2kSInt<PlainT> right;
  private final Spdz2kSInt<PlainT> product;

  public Spdz2kTriple(Spdz2kSInt<PlainT> left, Spdz2kSInt<PlainT> right,
      Spdz2kSInt<PlainT> product) {
    this.left = left;
    this.right = right;
    this.product = product;
  }

  public Spdz2kSInt<PlainT> getLeft() {
    return left;
  }

  public Spdz2kSInt<PlainT> getRight() {
    return right;
  }

  public Spdz2kSInt<PlainT> getProduct() {
    return product;
  }

}
