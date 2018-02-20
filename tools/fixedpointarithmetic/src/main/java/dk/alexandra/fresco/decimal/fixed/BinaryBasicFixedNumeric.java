package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.utils.Truncate;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public class BinaryBasicFixedNumeric extends BasicFixedNumeric {

  BinaryBasicFixedNumeric(ProtocolBuilderNumeric builder, int defaultPrecision, int maxPrecision) {
    super(builder, 2, defaultPrecision, maxPrecision);
  }

  BinaryBasicFixedNumeric(ProtocolBuilderNumeric builder) {
    this(builder, 16, 48);
  }

  @Override
  protected DRes<SInt> scale(ProtocolBuilderNumeric scope, DRes<SInt> n, int scale) {
    if (scale > 0) {
      n = scope.numeric().mult(BigInteger.ONE.shiftLeft(scale), n);
    } else if (scale < 0) {
      // In the binary case we use truncation instead of division which gives a significant
      // improvement in performance.
      n = scope.seq(new Truncate(n, -scale));
    }
    return n;
  }

}
