package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.OpenBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzLocalInversionProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputToAllProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocol4;
import dk.alexandra.fresco.suite.spdz.utils.SpdzFactory;

class SpdzBuilder implements BuilderFactoryNumeric {

  private SpdzFactory spdzFactory;

  SpdzBuilder(SpdzFactory spdzFactory) {
    this.spdzFactory = spdzFactory;
  }

  @Override
  public ProtocolFactory getProtocolFactory() {
    return spdzFactory;
  }

  @Override
  public BasicNumericFactory getBasicNumericFactory() {
    return spdzFactory;
  }

  @Override
  public NumericBuilder createNumericBuilder(ProtocolBuilder protocolBuilder) {
    return new NumericBuilder() {
      @Override
      public Computation<SInt> add(Computation<SInt> a, Computation<SInt> b) {
        SpdzSInt out = spdzFactory.getSInt();
        SpdzAddProtocol4 spdzAddProtocol4 = new SpdzAddProtocol4(a, b, out);
        protocolBuilder.append(spdzAddProtocol4);
        return spdzAddProtocol4;
      }


      @Override
      public Computation<SInt> add(OInt a, Computation<SInt> b) {
        return add(() -> spdzFactory.getSInt(a.getValue()), b);
      }


      @Override
      public Computation<SInt> sub(Computation<SInt> a, Computation<SInt> b) {
        SpdzSInt out = spdzFactory.getSInt();
        SpdzSubtractProtocol4 spdzSubtractProtocol4 = new SpdzSubtractProtocol4(a, b, out);
        protocolBuilder.append(spdzSubtractProtocol4);
        return spdzSubtractProtocol4;
      }

      @Override
      public Computation<SInt> sub(OInt a, Computation<SInt> b) {
        return sub(() -> spdzFactory.getSInt(a.getValue()), b);
      }

      @Override
      public Computation<SInt> sub(Computation<SInt> a, OInt b) {
        return sub(a, () -> spdzFactory.getSInt(b.getValue()));
      }

      @Override
      public Computation<SInt> mult(Computation<SInt> a, Computation<SInt> b) {
        SpdzSInt out = spdzFactory.getSInt();
        SpdzMultProtocol4 spdzMultProtocol4 = new SpdzMultProtocol4(a, b, out);
        protocolBuilder.append(spdzMultProtocol4);
        return spdzMultProtocol4;
      }

      @Override
      public Computation<SInt> mult(OInt a, Computation<SInt> b) {
        return () -> new SpdzSInt(((SpdzSInt) b.out()).value.multiply(a.getValue()));
      }

      @Override
      public Computation<SInt> createRandomSecretSharedBitProtocol() {
        return () -> spdzFactory.getRandomBitFromStorage();
      }

      @Override
      public Computation<OInt> invert(OInt oInt) {
        OInt out = spdzFactory.getOInt();
        SpdzLocalInversionProtocol spdzLocalInversionProtocol =
            new SpdzLocalInversionProtocol(oInt, out);
        protocolBuilder.append(spdzLocalInversionProtocol);
        return spdzLocalInversionProtocol;
      }
    };
  }

  @Override
  public OpenBuilder createOpenBuilder(ProtocolBuilder protocolBuilder) {
    return secretShare -> {
      OInt out = spdzFactory.getOInt();
      SpdzOutputToAllProtocol4 openProtocol = new SpdzOutputToAllProtocol4(secretShare, out);
      protocolBuilder.append(openProtocol);
      return openProtocol;
    };

  }
}
