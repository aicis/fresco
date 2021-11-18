package dk.alexandra.fresco.lib.fixed.truncations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.integer.binary.Truncate;
import java.math.BigInteger;

public class BinaryTruncation implements Truncation {

  private final int precision;
  private final BigInteger divisor;

  public BinaryTruncation(int precision) {
    this.precision = precision;
    this.divisor = BigInteger.ONE.shiftLeft(precision);
  }

  @Override
  public BigInteger getDivisor() {
    return divisor;
  }

  @Override
  public DRes<SInt> truncate(DRes<SInt> value, ProtocolBuilderNumeric scope) {
    return new Truncate(value, precision).buildComputation(scope);
  }
}
