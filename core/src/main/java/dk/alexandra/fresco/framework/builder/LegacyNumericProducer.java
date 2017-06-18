package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.FactoryNumericProducer;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;

public class LegacyNumericProducer<SIntT extends SInt> implements FactoryNumericProducer<SIntT> {

  private final ProtocolFactory protocolFactory;
  private BasicNumericFactory<SIntT> basicNumericFactory;

  public LegacyNumericProducer(BasicNumericFactory<SIntT> basicNumericFactory) {
    this.basicNumericFactory = basicNumericFactory;
    this.protocolFactory = basicNumericFactory;
  }

  @Override
  public ProtocolFactory getProtocolFactory() {
    return protocolFactory;
  }

  @Override
  public BasicNumericFactory<SIntT> getBasicNumericFactory() {
    return basicNumericFactory;
  }

  @Override
  public NumericBuilder<SIntT> createNumericBuilder(ProtocolBuilder sIntTProtocolBuilder) {
    return null;
  }
}
