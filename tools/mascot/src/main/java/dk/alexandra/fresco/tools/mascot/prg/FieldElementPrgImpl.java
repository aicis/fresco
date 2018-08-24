package dk.alexandra.fresco.tools.mascot.prg;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;


public class FieldElementPrgImpl implements FieldElementPrg {

  private final Drng drng;

  /**
   * Creates new FieldElement prg.
   *
   * @param seed seed to the underlying DRNG.
   */
  public FieldElementPrgImpl(StrictBitVector seed) {
    byte[] bytes = seed.toByteArray();
    if (bytes.length != AesCtrDrbg.SEED_LENGTH) {
      this.drng = new DrngImpl(AesCtrDrbgFactory.fromDerivedSeed(bytes));
    } else {
      this.drng = new DrngImpl(AesCtrDrbgFactory.fromRandomSeed(bytes));
    }
  }

  @Override
  public FieldElement getNext(BigInteger modulus) {
    return new FieldElement(drng.nextBigInteger(modulus), modulus);
  }

}
