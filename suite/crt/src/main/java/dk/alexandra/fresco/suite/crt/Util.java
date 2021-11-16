package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import java.math.BigInteger;
import java.util.Random;

public class Util {

  public static Pair<BigInteger, BigInteger> mapToCRT(BigInteger x, BigInteger p, BigInteger q) {
    return new Pair<>(x.mod(p), x.mod(q));
  }

  public static BigInteger mapToBigInteger(Pair<BigInteger, BigInteger> x, BigInteger p,
      BigInteger q) {
    BigInteger n1 = p.modInverse(q);
    BigInteger n2 = q.modInverse(p);
    BigInteger m = p.multiply(q);
    return n2.multiply(q).multiply(x.getFirst()).add(n1.multiply(p).multiply(x.getSecond())).mod(m);
  }

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

  public static Pair<DRes<SInt>, DRes<SInt>> split(DRes<SInt> crt) {
    CRTSInt asCRT = (CRTSInt) crt.out();
    return new Pair<>(asCRT.getLeft(), asCRT.getRight());

  }

}
