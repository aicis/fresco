package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.DRes;

/**
 * Generic interface representing open (public) real values. <p>Arithmetic suites supporting
 * arithmetic over reals must implement this interface. In some case this can just be a wrapper
 * around a {@link java.math.BigDecimal}, however, other suites may choose to rely on more efficient
 * implementations.</p>
 */
public interface OReal extends DRes<OReal> {

}
