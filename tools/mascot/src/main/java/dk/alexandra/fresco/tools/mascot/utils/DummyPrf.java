package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class DummyPrf implements Prf {

  @Override
  public FieldElement evaluate(StrictBitVector seed, BigInteger counter, BigInteger modulus,
      int bitLength) {
    // TODO: need to truncate down bit-length
    FieldElement el = new FieldElement(seed.toByteArray(), modulus, bitLength);
    FieldElement counterEl = new FieldElement(counter, modulus, bitLength);
    return el.add(counterEl);
  }

}
