package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.io.Serializable;
import java.math.BigInteger;

//todo maybe remove Addable interface from FieldElement
public interface FieldElement extends Serializable, Addable<FieldElement> {

  FieldElement add(FieldElement other);

  FieldElement subtract(FieldElement other);

  @Deprecated
  FieldElement negate();

  FieldElement multiply(FieldElement other);

  FieldElement pow(int exponent);

  FieldElement sqrt();

  FieldElement modInverse();

  // todo remove all methods under this

  @Deprecated
  boolean getBit(int bitIndex);

  @Deprecated
  byte[] toByteArray();

  //todo maybe move to field definition
  @Deprecated
  StrictBitVector toBitVector();

  @Deprecated
  BigInteger getModulus();
}
