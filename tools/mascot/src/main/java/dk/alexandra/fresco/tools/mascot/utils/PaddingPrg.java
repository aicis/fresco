package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

// TODO this class still isn't secure unless used with exactly 32 byte long seeds
public class PaddingPrg implements FieldElementPrg {

  Drng drng;

  /**
   * The seed to the underlying DRNG.
   * 
   * @param seed
   */
  public PaddingPrg(StrictBitVector seed) {
    this.drng = new DrngImpl(new AesCtrDrbg(toBytes(seed)));
  }

  /**
   * Converts bit vector to array of bytes. Pads if necessary.
   * 
   * @param seed
   * @return
   */
  static byte[] toBytes(StrictBitVector seed) {
    byte[] seedBytes = seed.toByteArray();
    if (seedBytes.length > 32) {
      throw new UnsupportedOperationException("Currently don't support seeds larger than 32 bytes");
    }
    byte[] padded = new byte[32];
    System.arraycopy(seedBytes, 0, padded, 0, seedBytes.length);
    return padded;
  }

  @Override
  public FieldElement getNext(BigInteger modulus, int bitLength) {
    FieldElement next = new FieldElement(drng.nextBigInteger(modulus), modulus, bitLength);
    return next;
  }

}
