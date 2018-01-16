package dk.alexandra.fresco.framework.util;

/**
 * This class wraps {@link AesCtrDrbg} which only supports fixed length seeds to work with any seed
 * shorter than the seed length required by {@link AesCtrDrbg} by padding the input seed up with 0
 * bytes.
 */
public class PaddingAesCtrDrbg implements Drbg {

  private final Drbg fixedLengthSeedDbrg;
  private final int requiredBitLength = 256;

  public PaddingAesCtrDrbg(byte[] seed) {
    if (seed.length * 8 > this.requiredBitLength) {
      throw new UnsupportedOperationException(
          "The length of the seed must be less than or equal to the required bit length of "
              + this.requiredBitLength);
    }
    this.fixedLengthSeedDbrg = new AesCtrDrbg(padUp(seed));
  }

  /**
   * Pads seed with zero bytes to be requiredByteLength bytes long.
   *
   * @param seed seed to pad
   * @return padded seed
   */
  byte[] padUp(byte[] seed) {
    byte[] padded = new byte[requiredBitLength / 8];
    System.arraycopy(seed, 0, padded, 0, seed.length);
    return padded;
  }

  @Override
  public void nextBytes(byte[] bytes) {
    fixedLengthSeedDbrg.nextBytes(bytes);
  }

}
