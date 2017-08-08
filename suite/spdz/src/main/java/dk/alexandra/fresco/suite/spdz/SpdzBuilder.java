package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzInputProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputToAllProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzRandomProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownRight;
import dk.alexandra.fresco.suite.spdz.utils.SpdzFactory;
import java.math.BigInteger;

class SpdzBuilder implements BuilderFactoryNumeric {

  private SpdzFactory spdzFactory;
  private MiscOIntGenerators miscOIntGenerators;

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
  public NumericBuilder createNumericBuilder(ProtocolBuilderNumeric protocolBuilder) {
    return new NumericBuilder() {
      @Override
      public Computation<SInt> add(Computation<SInt> a, Computation<SInt> b) {
        SpdzAddProtocol spdzAddProtocol = new SpdzAddProtocol(a, b);
        return protocolBuilder.append(spdzAddProtocol);
      }


      @Override
      public Computation<SInt> add(BigInteger a, Computation<SInt> b) {
        SpdzAddProtocolKnownLeft spdzAddProtocolKnownLeft = new SpdzAddProtocolKnownLeft(a, b);
        return protocolBuilder.append(spdzAddProtocolKnownLeft);
      }


      @Override
      public Computation<SInt> sub(Computation<SInt> a, Computation<SInt> b) {
        SpdzSubtractProtocol spdzSubtractProtocol = new SpdzSubtractProtocol(a, b);
        return protocolBuilder.append(spdzSubtractProtocol);
      }

      @Override
      public Computation<SInt> sub(BigInteger a, Computation<SInt> b) {
        SpdzSubtractProtocolKnownLeft spdzSubtractProtocolKnownLeft =
            new SpdzSubtractProtocolKnownLeft(a, b);
        return protocolBuilder.append(spdzSubtractProtocolKnownLeft);
      }

      @Override
      public Computation<SInt> sub(Computation<SInt> a, BigInteger b) {
        SpdzSubtractProtocolKnownRight spdzSubtractProtocolKnownRight =
            new SpdzSubtractProtocolKnownRight(a, b);
        return protocolBuilder.append(spdzSubtractProtocolKnownRight);
      }

      @Override
      public Computation<SInt> mult(Computation<SInt> a, Computation<SInt> b) {
        SpdzMultProtocol spdzMultProtocol = new SpdzMultProtocol(a, b);
        return protocolBuilder.append(spdzMultProtocol);
      }

      @Override
      public Computation<SInt> mult(BigInteger a, Computation<SInt> b) {
        SpdzMultProtocolKnownLeft spdzMultProtocol4 = new SpdzMultProtocolKnownLeft(a, b);
        return protocolBuilder.append(spdzMultProtocol4);

      }

      @Override
      public Computation<SInt> randomBit() {
        return () -> spdzFactory.getRandomBitFromStorage();
      }

      @Override
      public Computation<SInt> randomElement() {
        return protocolBuilder.append(new SpdzRandomProtocol());
      }

      @Override
      public Computation<SInt> known(BigInteger value) {
        return protocolBuilder.append(new SpdzKnownSIntProtocol(value, spdzFactory.getSInt()));
      }

      @Override
      public Computation<SInt> input(BigInteger value, int inputParty) {
        SpdzSInt out = spdzFactory.getSInt();
        SpdzInputProtocol protocol = new SpdzInputProtocol(value, out, inputParty);
        return protocolBuilder.append(protocol);
      }

      @Override
      public Computation<BigInteger> open(Computation<SInt> secretShare) {
        SpdzOutputToAllProtocol openProtocol = new SpdzOutputToAllProtocol(secretShare);
        return protocolBuilder.append(openProtocol);
      }

      @Override
      public Computation<SInt[]> getExponentiationPipe() {
        //TODO Should be a protocol
        return () -> spdzFactory.getExponentiationPipe();
      }
    };
  }

  @Override
  public MiscOIntGenerators getBigIntegerHelper() {
    if (miscOIntGenerators == null) {
      miscOIntGenerators = new MiscOIntGenerators(spdzFactory.getModulus());
    }
    return miscOIntGenerators;
  }
}
