package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderImpl;

/**
 * Protocol builder for binary applications. Applications can be given an instance of this class for
 * building their binary applications. This class includes all {@link ComputationDirectory} classes
 * known to FRESCO which are relevant for binary applications.
 *
 */
public class ProtocolBuilderBinary extends ProtocolBuilderImpl<ProtocolBuilderBinary> {

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

  /**
   * Creates a {@link Binary} computation directory for this instance - i.e. this intended producer.
   * Contains only protocol suite native operations such as XOR and AND. Basic operations which can
   * be created from XOR, AND and NOT (such as XNOR, NAND) are found using {@link #advancedBinary}.
   * 
   * @return The binary computation directory.
   */
  public Binary binary() {
    if (this.binaryBuilder == null) {
      this.binaryBuilder = this.factory.createBinary(this);
    }
    return this.binaryBuilder;
  }

  /**
   * Creates an {@link AdvancedBinary} computation directory for this instance - i.e. this intended
   * producer. Contains advanced binary protocols. These protocols include, but are not limited to,
   * addition and multiplication of numbers represented in binary form.
   * 
   * @return The advanced binary computation directory.
   */
  public AdvancedBinary advancedBinary() {
    if (this.advancedBinary == null) {
      this.advancedBinary = this.factory.createAdvancedBinary(this);
    }
    return this.advancedBinary;
  }

  /**
   * Creates a {@link Comparison} computation directory for this instance - i.e. this intended
   * producer. Contains protocols on comparing numbers in binary form.
   * 
   * @return The comparison computation directory.
   */
  public Comparison comparison() {
    if (this.comparison == null) {
      this.comparison = this.factory.createComparison(this);
    }
    return this.comparison;
  }

  /**
   * Creates a {@link BristolCrypto} computation directory for this instance - i.e. this intended
   * producer. Contains various cryptographic primitives such as AES and SHA-256.
   * 
   * @return The bristol crypto computation directory.
   */
  public BristolCrypto bristol() {
    if (this.bristolCrypto == null) {
      this.bristolCrypto = this.factory.createBristolCrypto(this);
    }
    return this.bristolCrypto;
  }

  /**
   * Creates a {@link Debug} computation directory for for this instance - i.e. this intended
   * producer. Contains debugging protocols for use during application development. <b>WARNING: Do
   * not use in production code as most methods within this builder reveals values to all
   * parties.</b>
   * 
   * @return The debug computation directory.
   */
  public Debug debug() {
    if (this.debug == null) {
      this.debug = this.factory.createDebug(this);
    }
    return this.debug;
  }
}
