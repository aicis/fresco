package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.builder.ProtocolBuilderImpl;
import java.util.function.Consumer;

public class ProtocolBuilderBinary extends
    ProtocolBuilderImpl<ProtocolBuilderBinary> {

  private BuilderFactoryBinary factory;
  private BinaryBuilderAdvanced binaryBuilderAdvanced;
  private ComparisonBuilderBinary comparisonBuilderBinary;
  private BristolCryptoBuilder bristolCryptoBuilder;
  private BinaryBuilder binaryBuilder;
  private BinaryDebugBuilder utilityBuilder;

  ProtocolBuilderBinary(BuilderFactoryBinary factory, boolean parallel) {
    super(factory, parallel);
    this.factory = factory;
  }

  /** @deprecated - protocol suite can do this themselves
   */
  @Deprecated
  public static ProtocolBuilderBinary createApplicationRoot(BuilderFactoryBinary factory,
      Consumer<ProtocolBuilderBinary> consumer) {
    ProtocolBuilderBinary builder = new ProtocolBuilderBinary(factory, false);
    builder.addConsumer(consumer, () -> new ProtocolBuilderBinary(factory, false));
    return builder;
  }

  public BinaryBuilder binary() {
    if (this.binaryBuilder == null) {
      this.binaryBuilder = this.factory.createBinaryBuilder(this);
    }
    return this.binaryBuilder;
  }

  public BinaryBuilderAdvanced advancedBinary() {
    if (this.binaryBuilderAdvanced == null) {
      this.binaryBuilderAdvanced = this.factory.createAdvancedBinary(this);
    }
    return this.binaryBuilderAdvanced;
  }

  public ComparisonBuilderBinary comparison() {
    if (this.comparisonBuilderBinary == null) {
      this.comparisonBuilderBinary = this.factory.createComparison(this);
    }
    return this.comparisonBuilderBinary;
  }

  public BristolCryptoBuilder bristol() {
    if (this.bristolCryptoBuilder == null) {
      this.bristolCryptoBuilder = this.factory.createBristolCryptoBuilder(this);
    }
    return this.bristolCryptoBuilder;
  }

  public BinaryDebugBuilder utility() {
    if (this.utilityBuilder == null) {
      this.utilityBuilder = this.factory.createDebugBuilder(this);
    }
    return this.utilityBuilder;
  }
}
