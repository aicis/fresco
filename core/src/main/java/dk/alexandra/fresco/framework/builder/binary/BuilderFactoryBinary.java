package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.BuilderFactory;

public interface BuilderFactoryBinary extends
    BuilderFactory<ProtocolBuilderBinary> {

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

  default BinaryUtilityBuilder createUtilityBuilder(ProtocolBuilderBinary builder) {
    return new DefaultBinaryUtilityBuilder(builder);
  }

  default ProtocolBuilderBinary createSequential() {
    return new ProtocolBuilderBinary(this, false);
  }

  default ProtocolBuilderBinary createParallel() {
    return new ProtocolBuilderBinary(this, true);
  }


}
