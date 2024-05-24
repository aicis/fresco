package dk.alexandra.fresco.lib.common.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.integer.mod.Mod2m;
import java.math.BigInteger;

/**
 * Given secret value a, computes a &lt;? 0.
 * https://www.researchgate.net/publication/225092133_Improved_Primitives_for_Secure_Multiparty_Integer_Computation
 */
public class LessThanZero implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int maxBitlength;

  /**
   * Constructs new {@link LessThanZero}.
   *
   * @param input input to compare to 0
   * @param maxBitlength number of bits to compare
   */
  public LessThanZero(DRes<SInt> input, int maxBitlength) {
    this.input = input;
    this.maxBitlength = maxBitlength;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    final int statisticalSecurity = builder.getBasicNumericContext()
        .getStatisticalSecurityParam();
    return builder.seq(seq -> {
      DRes<SInt> inputMod2m = seq.seq(new Mod2m(input, maxBitlength - 1, maxBitlength,
          statisticalSecurity));
      Numeric numeric = seq.numeric();
      DRes<SInt> difference = numeric.sub(input, inputMod2m);
      BigInteger twoToMinusM = BigInteger.ONE.shiftLeft(maxBitlength - 1)
          .modInverse(seq.getBasicNumericContext().getModulus());
      return numeric.sub(BigInteger.ZERO, numeric.mult(twoToMinusM, difference));
    });
  }

}