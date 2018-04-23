package dk.alexandra.fresco.framework.value;

import dk.alexandra.fresco.framework.DRes;

/**
 * Generic interface representing open (public) values. <p>Arithmetic suites must implement this
 * interface. In some case this can just be a wrapper around a {@link java.math.BigInteger},
 * however, other suites may choose to rely on more efficient implementations.</p>
 */
public interface OInt extends DRes<OInt> {

}
