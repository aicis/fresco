package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzInputProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputToAllProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzRandomProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocol4;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownRight;
import dk.alexandra.fresco.suite.spdz.utils.SpdzFactory;
import java.math.BigInteger;
import java.util.Objects;

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
  public NumericBuilder createNumericBuilder(ProtocolBuilderNumeric protocolBuilder) {
    return new NumericBuilder() {
      @Override
      public Computation<SInt> add(Computation<SInt> a, Computation<SInt> b) {
        SpdzAddProtocol4 spdzAddProtocol4 = new SpdzAddProtocol4(a, b);
        protocolBuilder.append(spdzAddProtocol4);
        return spdzAddProtocol4;
      }


      @Override
      public Computation<SInt> add(BigInteger a, Computation<SInt> b) {
        SpdzAddProtocolKnownLeft spdzAddProtocolKnownLeft = new SpdzAddProtocolKnownLeft(a, b);
        protocolBuilder.append(spdzAddProtocolKnownLeft);
        return spdzAddProtocolKnownLeft;
      }


      @Override
      public Computation<SInt> sub(Computation<SInt> a, Computation<SInt> b) {
        SpdzSubtractProtocol4 spdzSubtractProtocol4 = new SpdzSubtractProtocol4(a, b);
        protocolBuilder.append(spdzSubtractProtocol4);
        return spdzSubtractProtocol4;
      }

      @Override
      public Computation<SInt> sub(BigInteger a, Computation<SInt> b) {
        SpdzSubtractProtocolKnownLeft spdzSubtractProtocolKnownLeft =
            new SpdzSubtractProtocolKnownLeft(a, b);
        protocolBuilder.append(spdzSubtractProtocolKnownLeft);
        return spdzSubtractProtocolKnownLeft;
      }

      @Override
      public Computation<SInt> sub(Computation<SInt> a, BigInteger b) {
        SpdzSubtractProtocolKnownRight spdzSubtractProtocolKnownRight =
            new SpdzSubtractProtocolKnownRight(a, b);
        protocolBuilder.append(spdzSubtractProtocolKnownRight);
        return spdzSubtractProtocolKnownRight;
      }

      @Override
      public Computation<SInt> mult(Computation<SInt> a, Computation<SInt> b) {
        SpdzMultProtocol4 spdzMultProtocol4 = new SpdzMultProtocol4(a, b);
        protocolBuilder.append(spdzMultProtocol4);
        return spdzMultProtocol4;
      }

      @Override
      public Computation<SInt> mult(BigInteger a, Computation<SInt> b) {
        BigInteger notNullA = Objects.requireNonNull(a);
        return () -> {
          SpdzSInt left = (SpdzSInt) b.out();
          return new SpdzSInt(left.value.multiply(notNullA));
        };
      }

      @Override
      public Computation<SInt> randomBit() {
        return () -> spdzFactory.getRandomBitFromStorage();
      }

      @Override
      public Computation<SInt> randomElement() {
        return protocolBuilder.append(new SpdzRandomProtocol4());
      }

      @Override
      public Computation<SInt> known(BigInteger value) {
        return protocolBuilder.append(new SpdzKnownSIntProtocol(value, spdzFactory.getSInt()));
      }

      @Override
      public Computation<SInt> input(BigInteger value, int inputParty) {
        SpdzSInt out = spdzFactory.getSInt();
        SpdzInputProtocol protocol = new SpdzInputProtocol(value, out, inputParty);
        protocolBuilder.append(protocol);
        return protocol::out;
      }

      @Override
      public Computation<BigInteger> open(Computation<SInt> secretShare) {
        SpdzOutputToAllProtocol4 openProtocol = new SpdzOutputToAllProtocol4(secretShare);
        protocolBuilder.append(openProtocol);
        return openProtocol;
      }

      @Override
      public Computation<SInt[]> getExponentiationPipe() {
        //TODO Should be a protocol
        return () -> spdzFactory.getExponentiationPipe();
      }

      @Override
      public Computation<BigInteger[]> getExpFromOInt(BigInteger value, int maxExp) {
        return () -> spdzFactory.getExpFromOInt(value, maxExp);
      }
    };
  }

}
