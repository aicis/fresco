package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;

public interface FactoryNumericProducer<SIntT extends SInt> extends FactoryProducer {

  BasicNumericFactory<SIntT> getBasicNumericFactory();

  NumericBuilder<SIntT> createNumericBuilder(ProtocolBuilder sIntTProtocolBuilder);
}
