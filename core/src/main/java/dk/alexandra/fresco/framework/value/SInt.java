package dk.alexandra.fresco.framework.value;

import dk.alexandra.fresco.framework.DRes;

/**
 * Basic closed numeric value. Arithmetic protocol suites needs to implement this interface.
 * Typically, an implementation holds a share of the secret shared value.
 */
public interface SInt extends DRes<SInt> {

}
