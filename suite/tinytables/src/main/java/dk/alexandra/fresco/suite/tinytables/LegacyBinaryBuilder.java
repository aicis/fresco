package dk.alexandra.fresco.suite.tinytables;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

public class LegacyBinaryBuilder implements BuilderFactory<ProtocolBuilderBinary> {

  private final ProtocolFactory protocolFactory;
  private AbstractBinaryFactory basicBinaryFactory;

  public LegacyBinaryBuilder(AbstractBinaryFactory binaryFactory) {
    this.basicBinaryFactory = binaryFactory;
    this.protocolFactory = binaryFactory;
  }

  @Override
  public ProtocolFactory getProtocolFactory() {
    return protocolFactory;
  }

  @Override
  public ProtocolBuilderBinary createProtocolBuilder() {
    return ProtocolBuilderBinary.createApplicationRoot(basicBinaryFactory);
  }

}
