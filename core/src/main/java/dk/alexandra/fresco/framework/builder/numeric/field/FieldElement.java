package dk.alexandra.fresco.framework.builder.numeric.field;

import dk.alexandra.fresco.framework.builder.numeric.Addable;
import java.io.Serializable;

public interface FieldElement extends Serializable, Addable<FieldElement> {

  FieldElement subtract(FieldElement other);

  FieldElement negate();

  FieldElement multiply(FieldElement other);

  FieldElement sqrt();

  FieldElement modInverse();

}
