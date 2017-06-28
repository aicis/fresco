package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * This protocol is an implementation Euclidean division (finding quotient and
 * remainder) on integers with a secret shared divedend and a known divisor. In
 * the implementation we calculate a constant <i>m</i> such that multiplication
 * with <i>m</i> will correspond to the desired division -- just shifted a
 * number of bits to the left. To get the right result we just need to shift
 * back again.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class KnownDivisorRemainder implements ComputationBuilder<SInt> {

  private final Computation<SInt> dividend;
  private final BigInteger divisor;

  KnownDivisorRemainder(
      Computation<SInt> dividend,
      BigInteger divisor) {
    this.dividend = dividend;
    this.divisor = divisor;
  }

  @Override
  public Computation<SInt> build(SequentialProtocolBuilder builder) {
    AdvancedNumericBuilder advancedNumericBuilder = builder.createAdvancedNumericBuilder();
    Computation<SInt> divisionResult = advancedNumericBuilder.div(dividend, divisor);

    NumericBuilder numeric = builder.numeric();
    return numeric.sub(dividend, numeric.mult(divisor, divisionResult));
  }
}
