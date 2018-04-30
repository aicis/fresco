package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class Spdz2kTriple<PlainT extends CompUInt<?, ?, PlainT>> {

  private final Spdz2kSIntArithmetic<PlainT> left;
  private final Spdz2kSIntArithmetic<PlainT> right;
  private final Spdz2kSIntArithmetic<PlainT> product;

  public Spdz2kTriple(Spdz2kSIntArithmetic<PlainT> left, Spdz2kSIntArithmetic<PlainT> right,
      Spdz2kSIntArithmetic<PlainT> product) {
    this.left = left;
    this.right = right;
    this.product = product;
  }

  public Spdz2kSIntArithmetic<PlainT> getLeft() {
    return left;
  }

  public Spdz2kSIntArithmetic<PlainT> getRight() {
    return right;
  }

  public Spdz2kSIntArithmetic<PlainT> getProduct() {
    return product;
  }

}
