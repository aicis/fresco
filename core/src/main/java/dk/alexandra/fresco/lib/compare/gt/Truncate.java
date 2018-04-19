package dk.alexandra.fresco.lib.compare.gt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.mod.Mod2m;
import java.math.BigInteger;

/**
 * Given k-bit secret input a and value m < k computes a >> m.
 */
public class Truncate implements Computation<SInt, ProtocolBuilderNumeric> {

  private static BigInteger TWO_TO_MINUS_M;
  private final DRes<SInt> input;
  private final int m;
  private final int k;
  private final int kappa;

  public Truncate(DRes<SInt> input, int m, int k, int kappa) {
    this.input = input;
    this.m = m;
    this.k = k;
    this.kappa = kappa;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<SInt> inputMod2m = builder.seq(new Mod2m(input, m, k, kappa));
    DRes<SInt> difference = builder.numeric().sub(input, inputMod2m);
    BigInteger twoToMinusM = invertPowerOfTwo(m, builder.getBasicNumericContext().getModulus());
    return builder.numeric().mult(twoToMinusM, difference);
  }

  /**
   * Computes 2^{-m} % modulus.
   */
  private BigInteger invertPowerOfTwo(int m, BigInteger modulus) {
    // TODO this might belong on the numeric context
    if (TWO_TO_MINUS_M == null) {
      TWO_TO_MINUS_M = BigInteger.ONE.shiftLeft(m - 1).modInverse(modulus);
    }
    return TWO_TO_MINUS_M;
  }

}
