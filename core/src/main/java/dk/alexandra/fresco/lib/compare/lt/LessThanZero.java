package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.mod.Mod2m;
import java.math.BigInteger;

/**
 * Given secret value a, computes a <? 0.
 */
public class LessThanZero implements Computation<SInt, ProtocolBuilderNumeric> {
  // TODO add reference to protocol description

  private final DRes<SInt> input;

  /**
   * Constructs new {@link LessThanZero}.
   *
   * @param input input to compare to 0
   */
  public LessThanZero(DRes<SInt> input) {
    this.input = input;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    final int maxBitlength = builder.getBasicNumericContext().getMaxBitLength();
    final int statisticalSecurity = builder.getBasicNumericContext().getStatisticalSecurityParam();
    DRes<SInt> inputMod2m = builder.seq(new Mod2m(input, maxBitlength - 1, maxBitlength,
        statisticalSecurity));
    Numeric numeric = builder.numeric();
    DRes<SInt> difference = numeric.sub(input, inputMod2m);
    BigInteger twoToMinusM = builder.getBigIntegerHelper().invertPowerOfTwo(maxBitlength - 1);
    return numeric.sub(BigInteger.ZERO, numeric.mult(twoToMinusM, difference));
  }

}
