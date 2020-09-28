package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderImpl;

/**
 * Protocol builder for binary applications. Applications can be given an instance of this class for
 * building their binary applications. This class includes all {@link ComputationDirectory} classes
 * known to FRESCO which are relevant for binary applications.
 */
public class ProtocolBuilderBinary extends ProtocolBuilderImpl<ProtocolBuilderBinary> {

  private BuilderFactoryBinary factory;
  private Binary binaryBuilder;

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
}
