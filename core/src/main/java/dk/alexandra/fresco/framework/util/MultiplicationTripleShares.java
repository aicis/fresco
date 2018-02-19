package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;

/**
 * Generic representation of a multiplication triple.<p> A multiplication triple is a triple of the
 * form a, b, c where a * b = c (usually secret-shared). For each of the three values, this class
 * the open value as well as this party's share.</p>
 */
public class MultiplicationTripleShares {

  private final Pair<BigInteger, BigInteger> left;
  private final Pair<BigInteger, BigInteger> right;
  private final Pair<BigInteger, BigInteger> product;

  public MultiplicationTripleShares(
      Pair<BigInteger, BigInteger> left,
      Pair<BigInteger, BigInteger> right,
      Pair<BigInteger, BigInteger> product) {
    this.left = left;
    this.right = right;
    this.product = product;
  }

  public Pair<BigInteger, BigInteger> getLeft() {
    return left;
  }

  public Pair<BigInteger, BigInteger> getRight() {
    return right;
  }

  public Pair<BigInteger, BigInteger> getProduct() {
    return product;
  }
}
