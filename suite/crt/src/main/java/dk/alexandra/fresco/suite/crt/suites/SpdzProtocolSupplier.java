package dk.alexandra.fresco.suite.crt.suites;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticAddProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticCloseProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticKnownProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticMultProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticOpenProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticOpenToAllProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticRandomBitProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticRandomElementProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSubtractProtocol;
import dk.alexandra.fresco.suite.spdz.SpdzRandomBitProtocol;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzAddProtocolKnownLeft;
import dk.alexandra.fresco.suite.spdz.gates.SpdzInputProtocol;
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
import java.util.Random;

public class SpdzProtocolSupplier implements
    ProtocolSuiteProtocolSupplier<SpdzResourcePool> {

  @Override
  public NativeProtocol<SInt, SpdzResourcePool> known(BigInteger value) {
    return new SpdzKnownSIntProtocol(value);
  }

  @Override
  public NativeProtocol<SInt, SpdzResourcePool> input(BigInteger value, int playerId) {
    return new SpdzInputProtocol(value, playerId);
  }

  @Override
  public NativeProtocol<BigInteger, SpdzResourcePool> open(DRes<SInt> value, int playerId) {
    return new SpdzOutputSingleProtocol(value, playerId);
  }

  @Override
  public NativeProtocol<BigInteger, SpdzResourcePool> open(DRes<SInt> value) {
    return new SpdzOutputToAllProtocol(value);
  }

  @Override
  public NativeProtocol<SInt, SpdzResourcePool> add(DRes<SInt> a, DRes<SInt> b) {
    return new SpdzAddProtocol(a, b);
  }

  @Override
  public NativeProtocol<SInt, SpdzResourcePool> add(BigInteger a, DRes<SInt> b) {
    return new SpdzAddProtocolKnownLeft(a, b);
  }

  @Override
  public NativeProtocol<SInt, SpdzResourcePool> sub(DRes<SInt> a, DRes<SInt> b) {
    return new SpdzSubtractProtocol(a, b);
  }

  @Override
  public NativeProtocol<SInt, SpdzResourcePool> sub(DRes<SInt> a, BigInteger b) {
    return new SpdzSubtractProtocolKnownRight(a, b);
  }

  @Override
  public NativeProtocol<SInt, SpdzResourcePool> sub(BigInteger a, DRes<SInt> b) {
    return new SpdzSubtractProtocolKnownLeft(a, b);
  }

  @Override
  public NativeProtocol<SInt, SpdzResourcePool> mult(DRes<SInt> a, DRes<SInt> b) {
    return new SpdzMultProtocol(a, b);
  }

  @Override
  public NativeProtocol<SInt, SpdzResourcePool> mult(BigInteger a, DRes<SInt> b) {
    return new SpdzMultProtocolKnownLeft(a, b);
  }

  @Override
  public NativeProtocol<SInt, SpdzResourcePool> randomElement() {
    return new SpdzRandomProtocol();
  }

  @Override
  public NativeProtocol<SInt, SpdzResourcePool> randomBit() {
    return new SpdzRandomBitProtocol();
  }
}
