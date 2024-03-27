package dk.alexandra.fresco.framework.util;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A simple implementation based on a deterministic bit generator.
 */
public class DrngImpl implements Drng {
  private static final int RANDOMBUFFER_SIZE = 16;

  private Drbg drbg;
  private byte[] randomBytes = new byte[RANDOMBUFFER_SIZE];
  private int bitsLeft = 0;

  /**
   * Creates a number generator from a bit generator.
   * @param drbg a deterministic random bit generator. Not nullable.
   */
  public DrngImpl(Drbg drbg) {
    this.drbg = Objects.requireNonNull(drbg);
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

  @Override
  public boolean nextBit() {
    if (bitsLeft == 0) {
      drbg.nextBytes(randomBytes);
      bitsLeft = RANDOMBUFFER_SIZE * Byte.SIZE;
    }
    int index = RANDOMBUFFER_SIZE * Byte.SIZE - bitsLeft;
    byte currentByte = randomBytes[index / (Byte.SIZE)];
    byte currentBit = (byte) (currentByte >> (index % (Byte.SIZE)));
    bitsLeft--;
    return currentBit == 0x00 ? false : true;
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
