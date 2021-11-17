package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.ArithmeticDummyDataSupplier;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.Util;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import java.math.BigInteger;
import java.util.Random;
import java.util.function.Function;

public class CRTDummyDataSupplier implements CRTDataSupplier {

  private final FieldDefinition fp, fq;
  private final int myId;
  private final int players;
  private final Random random;
  private final Function<BigInteger, SInt> wrapperLeft, wrapperRight;

  public CRTDummyDataSupplier(int myId, int players, FieldDefinition fp,
      FieldDefinition fq, Function<BigInteger, SInt> wrapperLeft, Function<BigInteger, SInt> wrapperRight) {
    this.myId = myId;
    this.players = players;
    this.fp = fp;
    this.fq = fq;
    this.wrapperLeft = wrapperLeft;
    this.wrapperRight = wrapperRight;

    this.random = new Random(1234);

  }

  @Override
  public CRTSInt getCorrelatedNoise() {
    BigInteger r = Util.randomBigInteger(random, fp.getModulus());
    BigInteger l = Util
        .randomBigInteger(random, fq.getModulus().divide(fp.getModulus()).subtract(BigInteger.ONE));

    return new CRTSInt(wrapperLeft.apply(r),
        wrapperRight.apply(r.add(l.multiply(fp.getModulus()))));
  }

  @Override
  public CRTSInt getRandomBit() {
    BigInteger bit = Util.randomBigInteger(random, BigInteger.valueOf(2));
    return new CRTSInt(wrapperLeft.apply(bit),
        wrapperRight.apply(bit));
  }

  @Override
  public Pair<FieldDefinition, FieldDefinition> getFieldDefinitions() {
    return new Pair<>(fp, fq);
  }
}
