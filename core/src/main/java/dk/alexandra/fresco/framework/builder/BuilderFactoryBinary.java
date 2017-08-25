package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilder;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilderAdvanced;
import dk.alexandra.fresco.framework.builder.binary.BinaryUtilityBuilder;
import dk.alexandra.fresco.framework.builder.binary.BristolCryptoBuilder;
import dk.alexandra.fresco.framework.builder.binary.ComparisonBuilderBinary;
import dk.alexandra.fresco.framework.builder.binary.DefaultBinaryBuilderAdvanced;
import dk.alexandra.fresco.framework.builder.binary.DefaultBinaryUtilityBuilder;
import dk.alexandra.fresco.framework.builder.binary.DefaultBristolCryptoBuilder;
import dk.alexandra.fresco.framework.builder.binary.DefaultComparisonBinaryBuilder;

public interface BuilderFactoryBinary extends
    BuilderFactory<ProtocolBuilderBinary, ProtocolBuilderBinary> {

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
    BuilderFactoryBinary factory = this;
    return new ProtocolBuilderBinary(factory, false);
  }

  default ProtocolBuilderBinary createParallel() {
    BuilderFactoryBinary factory = this;
    return new ProtocolBuilderBinary(factory, true);
  }


}
