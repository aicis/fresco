package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.builder.numeric.Addable;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.io.Serializable;

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

  //todo maybe move to field definition
  @Deprecated
  StrictBitVector toBitVector();
}
