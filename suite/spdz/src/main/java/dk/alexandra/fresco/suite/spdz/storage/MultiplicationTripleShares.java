package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;

public class MultiplicationTripleShares {

  private final Pair<BigInteger,BigInteger> left;
  private final Pair<BigInteger,BigInteger> right;
  private final Pair<BigInteger,BigInteger> product;

  MultiplicationTripleShares(
      Pair<BigInteger,BigInteger> left,
      Pair<BigInteger,BigInteger> right,
      Pair<BigInteger,BigInteger> product) {
    this.left = left;
    this.right = right;
    this.product = product;
  }

  public Pair<BigInteger,BigInteger> getLeft() {
    return left;
  }

  public Pair<BigInteger,BigInteger> getRight() {
    return right;
  }

  public Pair<BigInteger,BigInteger> getProduct() {
    return product;
  }
}
