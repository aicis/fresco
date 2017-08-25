package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.ParallelBinaryBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;

public interface BuilderFactoryBinary extends
    BuilderFactory<SequentialBinaryBuilder, ParallelBinaryBuilder> {

  BinaryBuilder createBinaryBuilder(ProtocolBuilderBinary builder);

  BasicBinaryFactory createBasicBinaryFactory();

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

  default SequentialBinaryBuilder createSequential() {
    return ProtocolBuilderBinary.createApplicationRoot(this);
  }

  default ParallelBinaryBuilder createParallel() {
    return new ParallelBinaryBuilder(this);
  }


}
