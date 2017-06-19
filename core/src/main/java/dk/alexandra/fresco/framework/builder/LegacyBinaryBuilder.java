package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

public class LegacyBinaryBuilder implements BuilderFactory {

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

}
