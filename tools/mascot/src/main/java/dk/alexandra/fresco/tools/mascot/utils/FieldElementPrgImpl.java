package dk.alexandra.fresco.tools.mascot.utils;

import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
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
    this.drng = new DrngImpl(new PaddingAesCtrDrbg(seed.toByteArray()));
  }

  @Override
  public FieldElement getNext(BigInteger modulus, int bitLength) {
    FieldElement next = new FieldElement(drng.nextBigInteger(modulus), modulus, bitLength);
    return next;
  }

}
