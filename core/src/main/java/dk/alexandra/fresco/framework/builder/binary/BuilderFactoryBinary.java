package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.BuilderFactory;

public interface BuilderFactoryBinary extends BuilderFactory<ProtocolBuilderBinary> {

  BinaryBuilder createBinaryBuilder(ProtocolBuilderBinary builder);

  default ComparisonBuilderBinary createComparison(ProtocolBuilderBinary builder) {
    return new DefaultComparisonBinaryBuilder(builder);
  }

  default BinaryBuilderAdvanced createAdvancedBinary(ProtocolBuilderBinary builder) {
    return new DefaultBinaryBuilderAdvanced(builder);
  }

  default BristolCryptoBuilder createBristolCryptoBuilder(ProtocolBuilderBinary builder) {
    return new DefaultBristolCryptoBuilder(builder);
  }

  /**
   * Returns a builder which can be helpful while developing a new protocol. Be very careful though,
   * to include this in any production code since the debugging opens values to all parties.
   * 
   * @param builder
   * @return By default a standard debugger which opens values and prints them.
   */
  default BinaryDebugBuilder createDebugBuilder(ProtocolBuilderBinary builder) {
    return new DefaultBinaryDebugBuilder(builder);
  }

  default ProtocolBuilderBinary createSequential() {
    return new ProtocolBuilderBinary(this, false);
  }

  default ProtocolBuilderBinary createParallel() {
    return new ProtocolBuilderBinary(this, true);
  }


}
