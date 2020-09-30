package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;

/**
 * The core factory to implement when creating a binary protocol. Every {@link ComputationDirectory}
 * found in this factory will append the produced protocols to the supplied builder. Implementors
 * must provide a {@link Binary} - being directory for
 *
 * <ul>
 *   <li>simple, binary operations (XOR, AND)
 *   <li>Open operations for opening a small subset of values used in the control flow (is a&lt;b)&lt;
 *   <li>Factories for producing secret shared values
 * </ul>
 *
 * The other directories have defaults, based on the raw methods, but can be overridden if the
 * particular protocol suite has a more efficient way of e.g. comparing two numbers than a generic
 * approach would have.
 */
public interface BuilderFactoryBinary extends BuilderFactory<ProtocolBuilderBinary> {

  Binary createBinary(ProtocolBuilderBinary builder);

  default ProtocolBuilderBinary createSequential() {
    return new ProtocolBuilderBinary(this, false);
  }

  default ProtocolBuilderBinary createParallel() {
    return new ProtocolBuilderBinary(this, true);
  }
}
