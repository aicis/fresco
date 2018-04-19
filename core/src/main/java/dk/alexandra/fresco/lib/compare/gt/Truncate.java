package dk.alexandra.fresco.lib.compare.gt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.mod.DummyMod2m;
import java.math.BigInteger;

/**
 * Given k-bit secret input a and value m < k masks k - m upper bits and right shifts result by m.
 */
public class Truncate implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int m;
  private final int k;
  private final int kappa;

  /**
   * Constructs new {@link Truncate}.
   *
   * @param input input to be shifted
   * @param m shift by
   * @param k bit length of input
   * @param kappa computational security parameter
   */
  public Truncate(DRes<SInt> input, int m, int k, int kappa) {
    this.input = input;
    this.m = m;
    this.k = k;
    this.kappa = kappa;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<SInt> inputMod2m = builder.seq(new DummyMod2m(input, m, k, kappa));
    DRes<SInt> difference = builder.numeric().sub(input, inputMod2m);
    BigInteger twoToMinusM = builder.getBigIntegerHelper().invertPowerOfTwo(m);
    return builder.numeric().mult(twoToMinusM, difference);
  }

}
