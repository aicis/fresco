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
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputToAllProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzRandomProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownRight;
import dk.alexandra.fresco.suite.spdz.utils.SpdzFactory;
import java.math.BigInteger;
import java.util.Objects;

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
        protocolBuilder.append(spdzAddProtocol);
        return spdzAddProtocol;
      }


      @Override
      public Computation<SInt> add(BigInteger a, Computation<SInt> b) {
        SpdzAddProtocolKnownLeft spdzAddProtocolKnownLeft = new SpdzAddProtocolKnownLeft(a, b);
        protocolBuilder.append(spdzAddProtocolKnownLeft);
        return spdzAddProtocolKnownLeft;
      }


      @Override
      public Computation<SInt> sub(Computation<SInt> a, Computation<SInt> b) {
        SpdzSubtractProtocol spdzSubtractProtocol = new SpdzSubtractProtocol(a, b);
        protocolBuilder.append(spdzSubtractProtocol);
        return spdzSubtractProtocol;
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
        SpdzMultProtocol spdzMultProtocol = new SpdzMultProtocol(a, b);
        protocolBuilder.append(spdzMultProtocol);
        return spdzMultProtocol;
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
        protocolBuilder.append(protocol);
        return protocol::out;
      }

      @Override
      public Computation<BigInteger> open(Computation<SInt> secretShare) {
        SpdzOutputToAllProtocol openProtocol = new SpdzOutputToAllProtocol(secretShare);
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

  @Override
  public MiscOIntGenerators getBigIntegerHelper() {
    if (miscOIntGenerators == null) {
      miscOIntGenerators = new MiscOIntGenerators();
    }
    return miscOIntGenerators;
  }
}
