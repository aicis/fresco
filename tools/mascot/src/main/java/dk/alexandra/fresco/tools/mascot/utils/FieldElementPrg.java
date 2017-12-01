package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public interface FieldElementPrg {

  /**
   * Deterministically generates random field elements.
   * 
   * @param modulus
   * @param bitLength
   * @return
   */
  public FieldElement getNext(BigInteger modulus, int bitLength);
  
}
