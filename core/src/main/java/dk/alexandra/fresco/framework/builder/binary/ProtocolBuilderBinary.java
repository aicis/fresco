package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.builder.ProtocolBuilderImpl;

public class ProtocolBuilderBinary extends
    ProtocolBuilderImpl<ProtocolBuilderBinary> {

  private BuilderFactoryBinary factory;
  private Binary binaryBuilder;
  private AdvancedBinary advancedBinary;
  private Comparison comparison;
  private BristolCrypto bristolCrypto;
  private Debug debug;

  ProtocolBuilderBinary(BuilderFactoryBinary factory, boolean parallel) {
    super(factory, parallel);
    this.factory = factory;
  }

  public Binary binary() {
    if (this.binaryBuilder == null) {
      this.binaryBuilder = this.factory.createBinary(this);
    }
    return this.binaryBuilder;
  }

  public AdvancedBinary advancedBinary() {
    if (this.advancedBinary == null) {
      this.advancedBinary = this.factory.createAdvancedBinary(this);
    }
    return this.advancedBinary;
  }

  public Comparison comparison() {
    if (this.comparison == null) {
      this.comparison = this.factory.createComparison(this);
    }
    return this.comparison;
  }

  public BristolCrypto bristol() {
    if (this.bristolCrypto == null) {
      this.bristolCrypto = this.factory.createBristolCrypto(this);
    }
    return this.bristolCrypto;
  }

  public Debug debug() {
    if (this.debug == null) {
      this.debug = this.factory.createDebug(this);
    }
    return this.debug;
  }
}
