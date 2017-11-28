package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public interface Prf {

  /**
   * Deterministically generates random field element using the base seed and counter as a combined seed.
   * 
   * @param baseSeed makes up seed to prf together with counter
   * @param counter makes up seed to prf together with base seed
   * @param modulus prime modulus defining the field
   * @param bitLength the bit length of the prime modulus
   * @return random field element
   */
  public FieldElement evaluate(StrictBitVector baseSeed, BigInteger counter, BigInteger modulus,
      int bitLength);

}
