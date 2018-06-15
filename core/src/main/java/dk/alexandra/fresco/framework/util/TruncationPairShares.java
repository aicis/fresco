package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;

/**
 * Generic representation of a truncation pair. <p> A truncation pair is pre-processing material
 * used for probabilistic truncation. A truncation pair consists of a value r and r^{prime} such
 * that r^{prime} is a random element and r = r^{prime} / 2^{d}, i.e., r right-shifted by d.</p>
 */
public class TruncationPairShares {

  private final Pair<BigInteger, BigInteger> rPrime;
  private final Pair<BigInteger, BigInteger> r;

  public TruncationPairShares(Pair<BigInteger, BigInteger> rPrime,
      Pair<BigInteger, BigInteger> r) {
    this.rPrime = rPrime;
    this.r = r;
  }

  public Pair<BigInteger, BigInteger> getR() {
    return r;
  }

  public Pair<BigInteger, BigInteger> getRPrime() {
    return rPrime;
  }

}
