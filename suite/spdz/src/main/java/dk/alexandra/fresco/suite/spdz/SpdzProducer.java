package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.FactoryNumericProducer;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocol4;
import dk.alexandra.fresco.suite.spdz.utils.SpdzFactory;

class SpdzProducer implements FactoryNumericProducer<SpdzSInt> {

  private SpdzFactory spdzFactory;

  SpdzProducer(SpdzFactory spdzFactory) {
    this.spdzFactory = spdzFactory;
  }

  @Override
  public ProtocolFactory getProtocolFactory() {
    return spdzFactory;
  }

  @Override
  public BasicNumericFactory<SpdzSInt> getBasicNumericFactory() {
    return spdzFactory;
  }

  @Override
  public NumericBuilder<SpdzSInt> createNumericBuilder(ProtocolBuilder sIntTProtocolBuilder) {
    return new NumericBuilder<SpdzSInt>() {
      @Override
      public Computation<SpdzSInt> sub(Computation<SpdzSInt> a, Computation<SpdzSInt> b) {
        SpdzSInt out = spdzFactory.getSInt();
        return new SpdzSubtractProtocol4(a, b, out);
      }


      //  @Override
      public Computation<SpdzSInt> sub(OInt a, Computation<SpdzSInt> b) {
        return sub(() -> spdzFactory.getSInt(a.getValue()), b);
      }

      @Override
      public Computation<SpdzSInt> add(Computation<SpdzSInt> a, Computation<SpdzSInt> b) {
        SpdzSInt out = spdzFactory.getSInt();
        return new SpdzAddProtocol4(a, b, out);
      }

      @Override
      public Computation<SpdzSInt> mult(Computation<SpdzSInt> a, Computation<SpdzSInt> b) {
        SpdzSInt out = spdzFactory.getSInt();
        return new SpdzMultProtocol4(a, b, out);
      }

    };
  }
}
