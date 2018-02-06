package dk.alexandra.fresco.lib.math.integer.exp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Computes the exponentiation when the exponent is public. 
 */
public class ExponentiationOpenExponent implements
    Computation<SInt, ProtocolBuilderNumeric> {

  private DRes<SInt> base;
  private BigInteger exponent;

  public ExponentiationOpenExponent(DRes<SInt> x, BigInteger e) {
    this.base = x;
    this.exponent = e;
    if (exponent.compareTo(BigInteger.ZERO) <= 0) {
      throw new IllegalArgumentException(
          "This computation does not support exponent being equal to or less than 0");
    }
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq((seq) -> {
      DRes<SInt> accEven = base;
      return new IterationState(exponent, accEven, null);
    }).whileLoop(
        iterationState -> !iterationState.exponent.equals(BigInteger.ONE),
        (seq, iterationState) -> {
          BigInteger exponent = iterationState.exponent;
          DRes<SInt> accEven = iterationState.accEven;
          DRes<SInt> accOdd = iterationState.accOdd;
          Numeric numeric = seq.numeric();
          if (exponent.getLowestSetBit() == 0) {
            if (accOdd == null) {
              accOdd = accEven;
            } else {
              accOdd = numeric.mult(accOdd, accEven);
            }
            accEven = numeric.mult(accEven, accEven);
            exponent = exponent.subtract(BigInteger.ONE).shiftRight(1);
          } else {
            exponent = exponent.shiftRight(1);
            accEven = numeric.mult(accEven, accEven);
          }
          return new IterationState(exponent, accEven, accOdd);
        }
    ).seq((seq, iterationState) ->
        seq.numeric().mult(iterationState.accEven, iterationState.accOdd)
    );
  }

  private static class IterationState implements DRes<IterationState> {

    final BigInteger exponent;
    final DRes<SInt> accEven;
    final DRes<SInt> accOdd;

    private IterationState(BigInteger exponent,
        DRes<SInt> accEven,
        DRes<SInt> accOdd) {
      this.exponent = exponent;
      this.accEven = accEven;
      this.accOdd = accOdd;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }

}
