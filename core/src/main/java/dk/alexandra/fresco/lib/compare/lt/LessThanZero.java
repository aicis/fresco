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
  private final int k;
  private final int kappa;

  /**
   * Constructs new {@link LessThanZero}.
   *
   * @param input input to compare to 0
   * @param k bit length of input
   * @param kappa computational security parameter
   */
  public LessThanZero(DRes<SInt> input, int k, int kappa) {
    this.input = input;
    this.k = k;
    this.kappa = kappa;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<SInt> inputMod2m = builder.seq(new Mod2m(input, k - 1, k, kappa));
    Numeric numeric = builder.numeric();
    DRes<SInt> difference = numeric.sub(input, inputMod2m);
    BigInteger twoToMinusM = builder.getBigIntegerHelper().invertPowerOfTwo(k - 1);
    return numeric.sub(BigInteger.ZERO, numeric.mult(twoToMinusM, difference));
  }

}
