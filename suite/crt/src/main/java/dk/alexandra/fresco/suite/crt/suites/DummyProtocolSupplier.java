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
import java.math.BigInteger;
import java.util.Random;

public class DummyProtocolSupplier implements
    ProtocolSuiteProtocolSupplier<DummyArithmeticResourcePool> {

  private final Random random;
  private final FieldDefinition field;

  public DummyProtocolSupplier(Random random, FieldDefinition field) {
    this.random = random;
    this.field = field;
  }

  @Override
  public NativeProtocol<SInt, DummyArithmeticResourcePool> known(BigInteger value) {
    return new DummyArithmeticKnownProtocol(value);
  }

  @Override
  public NativeProtocol<SInt, DummyArithmeticResourcePool> input(BigInteger value, int playerId) {
    return new DummyArithmeticCloseProtocol(value, playerId);
  }

  @Override
  public NativeProtocol<BigInteger, DummyArithmeticResourcePool> open(DRes<SInt> value,
      int playerId) {
    return new DummyArithmeticOpenProtocol(value, playerId);
  }

  @Override
  public NativeProtocol<BigInteger, DummyArithmeticResourcePool> open(DRes<SInt> value) {
    return new DummyArithmeticOpenToAllProtocol(value);
  }

  @Override
  public NativeProtocol<SInt, DummyArithmeticResourcePool> add(DRes<SInt> a, DRes<SInt> b) {
    return new DummyArithmeticAddProtocol(a, b);
  }

  @Override
  public NativeProtocol<SInt, DummyArithmeticResourcePool> add(BigInteger a, DRes<SInt> b) {
    return new DummyArithmeticAddProtocol(new DummyArithmeticSInt(field.createElement(a)), b);
  }

  @Override
  public NativeProtocol<SInt, DummyArithmeticResourcePool> sub(DRes<SInt> a, DRes<SInt> b) {
    return new DummyArithmeticSubtractProtocol(a, b);
  }

  @Override
  public NativeProtocol<SInt, DummyArithmeticResourcePool> sub(DRes<SInt> a, BigInteger b) {
    return new DummyArithmeticSubtractProtocol(a, new DummyArithmeticSInt(field.createElement(b)));
  }

  @Override
  public NativeProtocol<SInt, DummyArithmeticResourcePool> sub(BigInteger a, DRes<SInt> b) {
    return new DummyArithmeticMultProtocol(new DummyArithmeticSInt(field.createElement(a)), b);
  }

  @Override
  public NativeProtocol<SInt, DummyArithmeticResourcePool> mult(DRes<SInt> a, DRes<SInt> b) {
    return new DummyArithmeticMultProtocol(a, b);
  }

  @Override
  public NativeProtocol<SInt, DummyArithmeticResourcePool> mult(BigInteger a, DRes<SInt> b) {
    return new DummyArithmeticMultProtocol(new DummyArithmeticSInt(field.createElement(a)), b);
  }

  @Override
  public NativeProtocol<SInt, DummyArithmeticResourcePool> randomElement() {
    return new DummyArithmeticRandomElementProtocol(random);
  }

  @Override
  public NativeProtocol<SInt, DummyArithmeticResourcePool> randomBit() {
    return new DummyArithmeticRandomBitProtocol(random);
  }

}
