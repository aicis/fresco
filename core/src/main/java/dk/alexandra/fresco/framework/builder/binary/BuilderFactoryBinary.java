package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.BuilderFactory;

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
   * @param builder
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
