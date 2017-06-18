package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;

public interface FactoryProducer<SIntT extends SInt> {

  ProtocolFactory getProtocolFactory();

  BasicNumericFactory<SIntT> getBasicNumericFactory();
}
