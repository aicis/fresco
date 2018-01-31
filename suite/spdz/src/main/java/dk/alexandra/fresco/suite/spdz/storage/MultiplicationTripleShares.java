package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.util.SameTypePair;
import java.math.BigInteger;

public class MultiplicationTripleShares {

  private final SameTypePair<BigInteger> left;
  private final SameTypePair<BigInteger> right;
  private final SameTypePair<BigInteger> product;

  MultiplicationTripleShares(
      SameTypePair<BigInteger> left,
      SameTypePair<BigInteger> right,
      SameTypePair<BigInteger> product) {
    this.left = left;
    this.right = right;
    this.product = product;
  }

  public SameTypePair<BigInteger> getLeft() {
    return left;
  }

  public SameTypePair<BigInteger> getRight() {
    return right;
  }

  public SameTypePair<BigInteger> getProduct() {
    return product;
  }
}
