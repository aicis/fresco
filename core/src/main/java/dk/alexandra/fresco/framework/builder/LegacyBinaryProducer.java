package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.FactoryProducer;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

public class LegacyBinaryProducer implements FactoryProducer {

  private final ProtocolFactory protocolFactory;
  private AbstractBinaryFactory basicBinaryFactory;

  public LegacyBinaryProducer(AbstractBinaryFactory binaryFactory) {
    this.basicBinaryFactory = binaryFactory;
    this.protocolFactory = binaryFactory;
  }

  @Override
  public ProtocolFactory getProtocolFactory() {
    return protocolFactory;
  }

}
