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
 * Given known value a and secret value b represented as bits, computes a &lt;? b.
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
    return builder.seq(seq -> {
      List<DRes<SInt>> secretBits = secretBitsDef.out();
      int numBits = secretBits.size();
      List<BigInteger> openBits = MathUtils.toBits(openValueA, numBits);
      Logical logical = Logical.using(seq);
      DRes<List<DRes<SInt>>> secretBitsNegated = logical.batchedNot(secretBitsDef);
      DRes<SInt> gt = seq
          .seq(new CarryOut(openBits, secretBitsNegated, BigInteger.ONE, true));
      return logical.not(gt);
    });
  }

}