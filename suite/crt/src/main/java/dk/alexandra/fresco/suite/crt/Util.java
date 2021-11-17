package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.util.Pair;
import java.math.BigInteger;
import java.util.Random;

public class Util {

  /** Map from F_m to F_p x F_q with m = pq */
  public static Pair<BigInteger, BigInteger> mapToCRT(BigInteger x, BigInteger p, BigInteger q) {
    return new Pair<>(x.mod(p), x.mod(q));
  }

  /** Map from F_p x F_q to F_m with m = pq */
  public static BigInteger mapToBigInteger(Pair<BigInteger, BigInteger> x, BigInteger p,
      BigInteger q) {
    if (x.getFirst() == null && x.getSecond() == null) {
      return null;
    }
    BigInteger n1 = p.modInverse(q);
    BigInteger n2 = q.modInverse(p);
    BigInteger m = p.multiply(q);
    return n2.multiply(q).multiply(x.getFirst()).add(n1.multiply(p).multiply(x.getSecond())).mod(m);
  }

  /** Map from F_p x F_q to F_m with m = pq */
  public static BigInteger mapToBigInteger(BigInteger x, BigInteger y, BigInteger p, BigInteger q) {
    return mapToBigInteger(new Pair<>(x, y), p, q);
  }

  /**
   * Sample a random integer in the range 0,...,u-1 (inclusive) with the given rng
   */
  public static BigInteger randomBigInteger(Random random, BigInteger u) {
    BigInteger r;
    do {
      r = new BigInteger(u.bitLength(), random);
    } while (r.compareTo(u) >= 0);
    return r;
  }

}
