package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;

/**
 * The core factory to implement when creating a binary protocol. Every {@link ComputationDirectory}
 * found in this factory will append the produced protocols to the supplied builder. Implementors
 * must provide a {@link Binary} - being directory for
 * <ul>
 * <li>simple, binary operations (XOR, AND)</li>
 * <li>Open operations for opening a small subset of values used in the control flow (is a<b)<</li>
 * <li>Factories for producing secret shared values</li>
 * </ul>
 * The other directories have defaults, based on the raw methods, but can be overridden if the
 * particular protocol suite has a more efficient way of e.g. comparing two numbers than a generic
 * approach would have.
 */
public interface BuilderFactoryBinary extends BuilderFactory<ProtocolBuilderBinary> {

  Binary createBinary(ProtocolBuilderBinary builder);

  default Comparison createComparison(ProtocolBuilderBinary builder) {
    return new DefaultComparison(builder);
  }

  default AdvancedBinary createAdvancedBinary(ProtocolBuilderBinary builder) {
    return new DefaultAdvancedBinary(builder);
  }

  default BristolCrypto createBristolCrypto(ProtocolBuilderBinary builder) {
    return new DefaultBristolCrypto(builder);
  }

  /**
   * Returns a builder which can be helpful while developing a new protocol. Be very careful though,
   * to include this in any production code since the debugging opens values to all parties.
   * 
   * @param builder The ProtocolBuilderBinary to use for constructing protocols
   * @return By default a standard debugger which opens values and prints them.
   */
  default Debug createDebug(ProtocolBuilderBinary builder) {
    return new DefaultDebug(builder);
  }

  default ProtocolBuilderBinary createSequential() {
    return new ProtocolBuilderBinary(this, false);
  }

  default ProtocolBuilderBinary createParallel() {
    return new ProtocolBuilderBinary(this, true);
  }


}
