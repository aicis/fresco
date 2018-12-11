package dk.alexandra.fresco.tools.mascot.prg;

import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.MascotFieldElement;
import java.math.BigInteger;

public class FieldElementPrgImpl implements FieldElementPrg {

  private final Drng drng;

  /**
   * Creates new MascotFieldElement prg.
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
  public MascotFieldElement getNext(BigInteger modulus) {
    return new MascotFieldElement(drng.nextBigInteger(modulus), modulus);
  }
}
