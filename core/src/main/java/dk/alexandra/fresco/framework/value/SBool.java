package dk.alexandra.fresco.framework.value;

import dk.alexandra.fresco.framework.DRes;

/**
 * Basic closed binary value. Binary protocol suites needs to implement this interface. Typically,
 * an implementation holds a share of the secret shared value.
 */
public interface SBool extends DRes<SBool> {

}
