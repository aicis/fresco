package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;

/**
 * A simple implementation based on a deterministic bit generator.
 */
public class DrngImpl implements Drng {

  private Drbg drbg;

  /**
   * Creates a number generator from a bit generator.
   * @param drbg a deterministic random bit generator
   */
  public DrngImpl(Drbg drbg) {
    this.drbg = drbg;
  }

  @Override
  public int nextInt(int limit) {
    return (int)nextLong(limit);
  }

  @Override
  public long nextLong(long limit) {
    if (limit < 1) {
      throw new IllegalArgumentException("Limit must be strictly positive, but is: " + limit);
    }
    int bitSize = (Long.SIZE - Long.numberOfLeadingZeros(limit - 1));
    byte[] bytes = getBytes(bitSize);
    long result = Byte.toUnsignedLong(bytes[0]);
    for (int i = 1; i < bytes.length; i++) {
      result <<= 8;
      result ^= Byte.toUnsignedLong(bytes[i]);
    }
    return result < limit ? result : nextLong(limit);
  }

  @Override
  public BigInteger nextBigInteger(BigInteger limit) {
    if (limit.signum() < 1) {
      throw new IllegalArgumentException("Limit must be strictly positive, but is: " + limit);
    }
    int bitSize = limit.bitLength();
    byte[] bytes = getBytes(bitSize);
    BigInteger result = new BigInteger(1, bytes);
    return result.compareTo(limit) < 0 ? result : nextBigInteger(limit);
  }

  private byte[] getBytes(int bitSize) {
    int residue = bitSize % 8;
    residue = (residue == 0) ? 8 : residue;
    int byteSize = bitSize / 8 + (1 - (residue / 8));
    byte[] bytes = new byte[byteSize];
    drbg.nextBytes(bytes);
    bytes[0] &= ~(0b11111111 << residue);
    return bytes;
  }

}
