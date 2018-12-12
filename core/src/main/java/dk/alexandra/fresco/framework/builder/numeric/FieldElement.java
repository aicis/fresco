package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;

public interface FieldElement extends Serializable {

  FieldElement add(FieldElement operand);

  FieldElement subtract(FieldElement operand);

  FieldElement multiply(FieldElement operand);

}
