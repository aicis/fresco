package dk.alexandra.fresco.framework.value;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;

/**
 * Basic closed numeric value. Arithmetic protocol suites needs to implement this interface.
 * Typically, an implementation holds a share of the secret shared value.
 */
public interface SInt extends DRes<SInt> {

  /**
   * Gets the share element.
   * @return a field element
   */
  FieldElement getShare();

  /**
   * Gets the mac element.
   * @return a field element
   */
  FieldElement getMac();
}
