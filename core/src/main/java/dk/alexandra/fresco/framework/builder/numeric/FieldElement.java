package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;

public interface FieldElement extends Comparable<FieldElement>, Serializable {

  FieldElement add(FieldElement operand);

  FieldElement subtract(FieldElement operand);

  FieldElement multiply(FieldElement operand);

  BigInteger convertToBigInteger();

  void toByteArray(byte[] bytes, int offset, int byteLength);
}
