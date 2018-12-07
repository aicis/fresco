package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;

public interface FieldElement extends Comparable<FieldElement>, Serializable {

  FieldElement add(FieldElement operand);

  FieldElement subtract(FieldElement operand);

  FieldElement multiply(FieldElement l);

  FieldElement divide(FieldElement denominator);

  FieldElement divide(int i);

  BigInteger convertValueToBigInteger();

  void toByteArray(byte[] bytes, int offset, int byteLength);
}
