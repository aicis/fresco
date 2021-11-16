package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.crt.Util;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import java.math.BigInteger;
import java.util.Random;

public class CRTDummyDataSupplier implements CRTDataSupplier {

  private final FieldDefinition fp, fq;
  private final int myId;
  private final int players;
  private final Random random;

  public CRTDummyDataSupplier(int myId, int players, FieldDefinition fp,
      FieldDefinition fq) {
    this.myId = myId;
    this.players = players;
    this.fp = fp;
    this.fq = fq;

    this.random = new Random(1234);
  }

  @Override
  public CRTSInt getCorrelatedNoise() {
    BigInteger r = Util.randomBigInteger(random, fp.getModulus());
    BigInteger l = Util
        .randomBigInteger(random, fq.getModulus().divide(fp.getModulus()).subtract(BigInteger.ONE));
    return new CRTSInt(new DummyArithmeticSInt(fp.createElement(r)),
        new DummyArithmeticSInt(fq.createElement(r.add(l.multiply(fp.getModulus())))));
  }

  @Override
  public CRTSInt getRandomBit() {
    return null;
  }

  @Override
  public Pair<FieldDefinition, FieldDefinition> getFieldDefinitions() {
    return new Pair<>(fp, fq);
  }
}
