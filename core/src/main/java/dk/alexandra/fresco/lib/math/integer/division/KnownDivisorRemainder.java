package dk.alexandra.fresco.lib.math.integer.division;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * This protocol is an implementation Euclidean division (finding quotient and
 * remainder) on integers with a secret shared divedend and a known divisor. In
 * the implementation we calculate a constant <i>m</i> such that multiplication
 * with <i>m</i> will correspond to the desired division -- just shifted a
 * number of bits to the left. To get the right result we just need to shift
 * back again.
 */
public class KnownDivisorRemainder implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> dividend;
  private final BigInteger divisor;

  public KnownDivisorRemainder(
      DRes<SInt> dividend,
      BigInteger divisor) {
    this.dividend = dividend;
    this.divisor = divisor;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    AdvancedNumeric advancedNumericBuilder = builder.advancedNumeric();
    DRes<SInt> divisionResult = advancedNumericBuilder.div(dividend, divisor);

    Numeric numeric = builder.numeric();
    return numeric.sub(dividend, numeric.mult(divisor, divisionResult));
  }
}
