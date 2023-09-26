package dk.alexandra.fresco.suite.crt.datatypes.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.crt.Util;
import dk.alexandra.fresco.suite.crt.datatypes.CRTCombinedPad;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import java.math.BigInteger;
import java.util.Random;
import java.util.function.Function;

public class CRTDummyDataSupplier<L extends NumericResourcePool,R extends NumericResourcePool> extends CRTDataSupplier<L,R> {

  private final FieldDefinition fp, fq;
  private final int players;
  private final int statisticalSecurity;
  private final Random random;
  private final Function<BigInteger, SInt> wrapperLeft, wrapperRight;

  // todo could aggregate the semihonest Dummy data supplier to avoid code-copy
  public CRTDummyDataSupplier(int myId, int players, int statisticalSecurity, FieldDefinition leftField,
                              FieldDefinition rightField, Function<BigInteger, SInt> wrapperLeft,
                              Function<BigInteger, SInt> wrapperRight) {
    super(null);
    this.players = players;
    this.statisticalSecurity = statisticalSecurity;
    this.fp = leftField;
    this.fq = rightField;
    this.wrapperLeft = wrapperLeft;
    this.wrapperRight = wrapperRight;

    this.random = new Random(1234);

  }

  @Override
  public DRes<CRTCombinedPad> getCorrelatedNoise(ProtocolBuilderNumeric builder) {
    BigInteger r = Util.randomBigInteger(random, fp.getModulus());
    BigInteger l = Util
        .randomBigInteger(random, BigInteger.valueOf(players));
    BigInteger rho = new BigInteger(statisticalSecurity, random);
    BigInteger psi = new BigInteger(statisticalSecurity, random);
    return DRes.of(
            new CRTCombinedPad(
                    new CRTSInt(wrapperLeft.apply(r),
        wrapperRight.apply(r.add(l.multiply(fp.getModulus())))),
                    wrapperRight.apply(rho), wrapperRight.apply(psi)));
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
