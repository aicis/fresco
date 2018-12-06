package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;

public interface FieldElement extends Comparable<FieldElement>, Serializable {

  FieldElement modInverse(BigInteger operand);

  FieldElement add(FieldElement operand);

  FieldElement subtract(FieldElement operand);

  FieldElement multiply(FieldElement l);

  FieldElement divide(FieldElement denominator);

  FieldElement divide(int i);

  BigInteger asBigInteger();

  void toByteArray(byte[] bytes, int offset, int byteLength);

  FieldElement modPow(FieldElement valueOf, BigInteger modulus);

}
