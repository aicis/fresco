package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.FactoryProducer;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

public class LegacyProducer<SIntT extends SInt> implements FactoryProducer<SIntT> {

  private final ProtocolFactory protocolFactory;
  private BasicNumericFactory<SIntT> basicNumericFactory;
  private AbstractBinaryFactory basicBinaryFactory;

  public LegacyProducer(BasicNumericFactory<SIntT> basicNumericFactory) {
    this.basicNumericFactory = basicNumericFactory;
    this.protocolFactory = basicNumericFactory;
  }

  public LegacyProducer(AbstractBinaryFactory binaryFactory) {
    this.basicBinaryFactory = binaryFactory;
    this.protocolFactory = binaryFactory;
  }

  @Override
  public ProtocolFactory getProtocolFactory() {
    return protocolFactory;
  }

  @Override
  public BasicNumericFactory<SIntT> getBasicNumericFactory() {
    return basicNumericFactory;
  }
}
