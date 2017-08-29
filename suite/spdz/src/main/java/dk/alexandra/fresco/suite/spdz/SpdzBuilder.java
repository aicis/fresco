package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumeric;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzInputProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputToAllProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzRandomProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownRight;
import dk.alexandra.fresco.suite.spdz.utils.SpdzNumeric;
import java.math.BigInteger;

class SpdzBuilder implements BuilderFactoryNumeric {

  private SpdzNumeric spdzFactory;
  private MiscOIntGenerators miscOIntGenerators;

  SpdzBuilder(SpdzNumeric spdzFactory) {
    this.spdzFactory = spdzFactory;
  }

  @Override
  public BasicNumeric getBasicNumericFactory() {
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
        return protocolBuilder.append(new SpdzRandomBitProtocol());
      }

      @Override
      public Computation<SInt> randomElement() {
        return protocolBuilder.append(new SpdzRandomProtocol());
      }

      @Override
      public Computation<SInt> known(BigInteger value) {
        return protocolBuilder.append(new SpdzKnownSIntProtocol(value));
      }

      @Override
      public Computation<SInt> input(BigInteger value, int inputParty) {
        SpdzInputProtocol protocol = new SpdzInputProtocol(value, inputParty);
        return protocolBuilder.append(protocol);
      }

      @Override
      public Computation<BigInteger> open(Computation<SInt> secretShare) {
        SpdzOutputToAllProtocol openProtocol = new SpdzOutputToAllProtocol(secretShare);
        return protocolBuilder.append(openProtocol);
      }

      @Override
      public Computation<BigInteger> open(Computation<SInt> secretShare, int outputParty) {
        SpdzOutputProtocol openProtocol = new SpdzOutputProtocol(secretShare, outputParty);
        return protocolBuilder.append(openProtocol);
      }

      @Override
      public Computation<SInt[]> getExponentiationPipe() {
        return protocolBuilder.append(new SpdzExponentiationPipeProtocol());
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
