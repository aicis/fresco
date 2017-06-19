package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.OpenBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzOInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzLocalInversionProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputToAllProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocol4;
import dk.alexandra.fresco.suite.spdz.utils.SpdzFactory;

class SpdzBuilder implements BuilderFactoryNumeric<SpdzSInt> {

  private SpdzFactory spdzFactory;

  SpdzBuilder(SpdzFactory spdzFactory) {
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
  public NumericBuilder<SpdzSInt> createNumericBuilder(ProtocolBuilder protocolBuilder) {
    return new NumericBuilder<SpdzSInt>() {
      @Override
      public Computation<SpdzSInt> add(Computation<SpdzSInt> a, Computation<SpdzSInt> b) {
        SpdzSInt out = spdzFactory.getSInt();
        SpdzAddProtocol4 spdzAddProtocol4 = new SpdzAddProtocol4(a, b, out);
        protocolBuilder.append(spdzAddProtocol4);
        return spdzAddProtocol4;
      }


      @Override
      public Computation<SpdzSInt> add(OInt a, Computation<SpdzSInt> b) {
        return add(() -> spdzFactory.getSInt(a.getValue()), b);
      }


      @Override
      public Computation<SpdzSInt> sub(Computation<SpdzSInt> a, Computation<SpdzSInt> b) {
        SpdzSInt out = spdzFactory.getSInt();
        SpdzSubtractProtocol4 spdzSubtractProtocol4 = new SpdzSubtractProtocol4(a, b, out);
        protocolBuilder.append(spdzSubtractProtocol4);
        return spdzSubtractProtocol4;
      }

      @Override
      public Computation<SpdzSInt> sub(OInt a, Computation<SpdzSInt> b) {
        return sub(() -> spdzFactory.getSInt(a.getValue()), b);
      }

      @Override
      public Computation<SpdzSInt> sub(Computation<SpdzSInt> a, OInt b) {
        return sub(a, () -> spdzFactory.getSInt(b.getValue()));
      }

      @Override
      public Computation<SpdzSInt> mult(Computation<SpdzSInt> a, Computation<SpdzSInt> b) {
        SpdzSInt out = spdzFactory.getSInt();
        SpdzMultProtocol4 spdzMultProtocol4 = new SpdzMultProtocol4(a, b, out);
        protocolBuilder.append(spdzMultProtocol4);
        return spdzMultProtocol4;
      }

      @Override
      public Computation<SpdzSInt> mult(OInt a, Computation<SpdzSInt> b) {
        return () -> new SpdzSInt(b.out().value.multiply(a.getValue()));
      }

      @Override
      public Computation<SpdzSInt> createRandomSecretSharedBitProtocol() {
        return () -> spdzFactory.getRandomBitFromStorage();
      }

      @Override
      public Computation<SpdzOInt> invert(OInt anInt) {
        OInt out = spdzFactory.getOInt();
        SpdzLocalInversionProtocol spdzLocalInversionProtocol =
            new SpdzLocalInversionProtocol((SpdzOInt) anInt, (SpdzOInt) out);
        protocolBuilder.append(spdzLocalInversionProtocol);
        return spdzLocalInversionProtocol;
      }
    };
  }

  @Override
  public OpenBuilder<SpdzSInt> createOpenBuilder(ProtocolBuilder protocolBuilder) {
    return new OpenBuilder<SpdzSInt>() {
      @Override
      public Computation<OInt> open(Computation<SpdzSInt> secretShare) {
        OInt out = spdzFactory.getOInt();
        SpdzOutputToAllProtocol4 openProtocol = new SpdzOutputToAllProtocol4(secretShare, out);
        protocolBuilder.append(openProtocol);
        Computation<OInt> openProtocol1 = (Computation) openProtocol;
        return openProtocol1;
      }
    };

  }
}
