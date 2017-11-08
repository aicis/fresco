package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public interface PRF {

  public FieldElement evaluate(BigInteger inputA, BigInteger inputB, BigInteger modulus,
      int bitLength);

}
