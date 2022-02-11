package dk.alexandra.fresco.lib.common.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.MathUtils;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.logical.Logical;
import java.math.BigInteger;
import java.util.List;

/**
 * Given known value a and secret value b represented as bits, computes a <? b.
 */
public class BitLessThanOpen implements Computation<SInt, ProtocolBuilderNumeric> {

  private final BigInteger openValueA;
  private final DRes<List<DRes<SInt>>> secretBitsDef;

  public BitLessThanOpen(BigInteger openValue, DRes<List<DRes<SInt>>> secretBits) {
    this.openValueA = openValue;
    this.secretBitsDef = secretBits;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SInt>> secretBits = secretBitsDef.out();
    int numBits = secretBits.size();
    List<BigInteger> openBits = MathUtils.toBits(openValueA, numBits);
    DRes<List<DRes<SInt>>> secretBitsNegated = Logical.using(builder).batchedNot(secretBitsDef);
    DRes<SInt> gt = builder
        .seq(new CarryOut(openBits, secretBitsNegated, BigInteger.ONE, true));
    return Logical.using(builder).not(gt);
  }

}