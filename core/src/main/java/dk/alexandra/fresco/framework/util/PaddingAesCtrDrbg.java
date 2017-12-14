package dk.alexandra.fresco.framework.util;

public class PaddingAesCtrDrbg implements Drbg {

  Drbg fixedLengthSeedDbrg;
  int requiredBitLength;

  public PaddingAesCtrDrbg(byte[] seed, int requiredBitLength) {
    if (seed.length * 8 > requiredBitLength) {
      throw new UnsupportedOperationException("Currently don't support seeds larger than 32 bytes");
    }
    this.requiredBitLength = requiredBitLength;
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
