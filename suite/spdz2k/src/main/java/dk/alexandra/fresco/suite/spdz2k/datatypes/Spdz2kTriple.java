package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class Spdz2kTriple<PlainT extends CompUInt<?, ?, PlainT>, SIntT extends Spdz2kSInt<PlainT>> {

  private final SIntT left;
  private final SIntT right;
  private final SIntT product;

  public Spdz2kTriple(SIntT left, SIntT right, SIntT product) {
    this.left = left;
    this.right = right;
    this.product = product;
  }

  public SIntT getLeft() {
    return left;
  }

  public SIntT getRight() {
    return right;
  }

  public SIntT getProduct() {
    return product;
  }

  @Override
  public String toString() {
    return "Spdz2kTriple{" +
        "left=" + left +
        ", right=" + right +
        ", product=" + product +
        '}';
  }
}
