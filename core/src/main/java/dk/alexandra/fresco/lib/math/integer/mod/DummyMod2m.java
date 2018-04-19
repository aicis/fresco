package dk.alexandra.fresco.lib.math.integer.mod;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

@Deprecated
public class DummyMod2m implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int m;
  private final int k;
  private final int kappa;

  /**
   * Constructs new {@link Mod2m}.
   *
   * @param input
   *          value to reduce
   * @param m
   *          exponent (2^{m})
   * @param k
   *          bitlength of the input
   * @param kappa
   *          Computational security parameter
   */
  public DummyMod2m(DRes<SInt> input, int m, int k, int kappa) {
    this.input = input;
    this.m = m;
    this.k = k;
    this.kappa = kappa;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<BigInteger> openInputDef = builder.numeric().open(input);
    return builder.seq(seq -> {
      BigInteger openInput = openInputDef.out();
      BigInteger twoToM = BigInteger.ONE.shiftLeft(m - 1);
      return seq.numeric().input(openInput.mod(twoToM), 1);
    });
  }

}
