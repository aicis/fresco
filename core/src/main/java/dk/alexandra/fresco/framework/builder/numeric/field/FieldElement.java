package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.builder.numeric.Addable;
import java.io.Serializable;

//todo maybe remove Addable interface from FieldElement to avoid sum functions
public interface FieldElement extends Serializable, Addable<FieldElement> {

  FieldElement add(FieldElement other);

  FieldElement subtract(FieldElement other);

  FieldElement negate();

  FieldElement multiply(FieldElement other);

  FieldElement sqrt();

  FieldElement modInverse();

}
