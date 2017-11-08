package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public class DummyPRF implements PRF {

  @Override
  public FieldElement evaluate(BigInteger inputA, BigInteger inputB, BigInteger modulus,
      int bitLength) {
    BigInteger raw = inputA.add(inputB).mod(modulus);
    return new FieldElement(raw, modulus, bitLength);
  }

}
