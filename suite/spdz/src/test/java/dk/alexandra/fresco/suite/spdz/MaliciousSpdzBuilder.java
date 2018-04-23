package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.lib.real.RealNumericContext;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzKnownSIntProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzMultProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputSingleProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputToAllProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzRandomProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzSubtractProtocolKnownRight;
import java.math.BigInteger;

public class MaliciousSpdzBuilder extends SpdzBuilder {

  MaliciousSpdzBuilder(BasicNumericContext spdzFactory, RealNumericContext realNumericContext) {
    super(spdzFactory, realNumericContext);
  }

  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric protocolBuilder) {
    return new Numeric() {
      @Override
      public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
        SpdzAddProtocol spdzAddProtocol = new SpdzAddProtocol(a, b);
        return protocolBuilder.append(spdzAddProtocol);
      }


      @Override
      public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
        SpdzAddProtocolKnownLeft spdzAddProtocolKnownLeft = new SpdzAddProtocolKnownLeft(a, b);
        return protocolBuilder.append(spdzAddProtocolKnownLeft);
      }

      @Override
      public DRes<SInt> addOpen(DRes<OInt> a, DRes<SInt> b) {
        throw new UnsupportedOperationException();
      }


      @Override
      public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
        SpdzSubtractProtocol spdzSubtractProtocol = new SpdzSubtractProtocol(a, b);
        return protocolBuilder.append(spdzSubtractProtocol);
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        SpdzSubtractProtocolKnownLeft spdzSubtractProtocolKnownLeft =
            new SpdzSubtractProtocolKnownLeft(a, b);
        return protocolBuilder.append(spdzSubtractProtocolKnownLeft);
      }

      @Override
      public DRes<SInt> subFromOpen(DRes<OInt> a, DRes<SInt> b) {
        throw new UnsupportedOperationException();
      }

      @Override
      public DRes<SInt> subOpen(DRes<SInt> a, DRes<OInt> b) {
        throw new UnsupportedOperationException();
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
        SpdzSubtractProtocolKnownRight spdzSubtractProtocolKnownRight =
            new SpdzSubtractProtocolKnownRight(a, b);
        return protocolBuilder.append(spdzSubtractProtocolKnownRight);
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
        SpdzMultProtocol spdzMultProtocol = new SpdzMultProtocol(a, b);
        return protocolBuilder.append(spdzMultProtocol);
      }

      @Override
      public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
        SpdzMultProtocolKnownLeft spdzMultProtocol4 = new SpdzMultProtocolKnownLeft(a, b);
        return protocolBuilder.append(spdzMultProtocol4);
      }

      @Override
      public DRes<SInt> multByOpen(DRes<OInt> a, DRes<SInt> b) {
        throw new UnsupportedOperationException();
      }

      @Override
      public DRes<SInt> randomBit() {
        return protocolBuilder.append(new SpdzRandomBitProtocol());
      }

      @Override
      public DRes<SInt> randomElement() {
        return protocolBuilder.append(new SpdzRandomProtocol());
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
        return protocolBuilder.append(new SpdzKnownSIntProtocol(value));
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
        return protocolBuilder.append(new MaliciousSpdzInputProtocol(value, inputParty));
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        SpdzOutputToAllProtocol openProtocol = new SpdzOutputToAllProtocol(secretShare);
        return protocolBuilder.append(openProtocol);
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        SpdzOutputSingleProtocol openProtocol =
            new SpdzOutputSingleProtocol(secretShare, outputParty);
        return protocolBuilder.append(openProtocol);
      }
    };
  }

}
