package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public interface Prf {

  public FieldElement evaluate(StrictBitVector seed, BigInteger counter, BigInteger modulus,
      int bitLength);

}
