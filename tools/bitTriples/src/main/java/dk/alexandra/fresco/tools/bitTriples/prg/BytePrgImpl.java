package dk.alexandra.fresco.tools.bitTriples.prg;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public class BytePrgImpl implements BytePrg {

  private final Drbg drbg;

  /**
   * Creates new byte prg.
   *
   * @param seed seed to the underlying DRNG.
   */
  public BytePrgImpl(StrictBitVector seed) {
    byte[] bytes = seed.toByteArray();
    drbg = AesCtrDrbgFactory.fromDerivedSeed(bytes);
  }
  /**
   * Creates new byte prg.
   *
   * @param drbg the drbg.
   */
  public BytePrgImpl(Drbg drbg) {
    this.drbg = drbg;
  }

  @Override
  public StrictBitVector getNext(int size) {
    return new StrictBitVector(size, drbg);
  }
}
