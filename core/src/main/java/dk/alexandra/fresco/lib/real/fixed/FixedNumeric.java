package dk.alexandra.fresco.lib.real.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.utils.Truncate;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An implementation of the {@link RealNumeric} ComputationDirectory based on a fixed point
 * representation of real numbers.
 */
public class FixedNumeric implements RealNumeric {

  private static final BigInteger TWO = BigInteger.valueOf(2);
  private final ProtocolBuilderNumeric builder;

  /**
   * Creates a new fixed point based RealNumeric ComputationDirectory
   *
   * @param builder a ProtocolBuilder for the numeric computations which will be used to implement
   *     the fixed point operations.
   */
  public FixedNumeric(ProtocolBuilderNumeric builder) {
    Objects.requireNonNull(builder);
    this.builder = builder;
  }

  /**
   * Return value * 2^{scale} rounded to the nearest integer, eg. the best integer for representing
   * a given value as a fixed point in base two with the given precision.
   * 
   * @param value A decimal value.
   * @param precision
   * @return
   */
  private static BigInteger unscaled(BigDecimal value, int precision) {
    return value.multiply(new BigDecimal(TWO.pow(precision))).setScale(0, RoundingMode.HALF_UP)
        .toBigIntegerExact();
  }

  /**
   * Return the value represented by the fixed point representation <i>unscaled *
   * 2<sup>-scale</sup></i>.
   *
   * @param unscaled The unscaled value
   * @param scale The scale
   * @return the value <i>unscaled * 2<sup>-scale</sup></i>.
   */
  private static BigDecimal scaled(
      FieldDefinition fieldDefinition,
      BigInteger unscaled, int scale) {
    return new BigDecimal(fieldDefinition.convertToSigned(unscaled))
        .setScale(scale, RoundingMode.UNNECESSARY)
        .divide(new BigDecimal(TWO.pow(scale)), RoundingMode.HALF_UP);
  }

  /**
   * Scale the given secret integer <i>n</i>. This is equivalent to multiplying <i>n</i> with
   * <i>2<sup>scale</sup></i>. Note that <i>n</i> may be negative.
   *
   * @param scope a builder used to build the required computations
   * @param n a secret integer
   * @param scale the scale
   * @return a DRes eventually holding the scaled value
   */
  private DRes<SInt> scale(ProtocolBuilderNumeric scope, DRes<SInt> n, int scale) {
    if (scale > 0) {
      n = scope.numeric().mult(BigInteger.ONE.shiftLeft(scale), n);
    } else if (scale < 0) {
      n = scope.seq(new Truncate(n, -scale));
    }
    return n;
  }

  @Override
  public DRes<SReal> add(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed fixedA = (SFixed) a.out();
      SFixed fixedB = (SFixed) b.out();
      return new SFixed(seq.numeric().add(fixedA.getSInt(), fixedB.getSInt()));
    });
  }

  @Override
  public DRes<SReal> add(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigInteger intA = unscaled(a, seq.getRealNumericContext().getPrecision());
      SFixed fixedB = (SFixed) b.out();
      return new SFixed(seq.numeric().add(intA, fixedB.getSInt()));
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed fixedA = (SFixed) a.out();
      SFixed fixedB = (SFixed) b.out();
      return new SFixed(seq.numeric().sub(fixedA.getSInt(), fixedB.getSInt()));
    });
  }

  @Override
  public DRes<SReal> sub(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigInteger scaledA = unscaled(a, seq.getRealNumericContext().getPrecision());
      SFixed fixedB = (SFixed) b.out();
      return new SFixed(seq.numeric().sub(scaledA, fixedB.getSInt()));
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFixed fixedA = (SFixed) a.out();
      BigInteger unscaledB = unscaled(b, seq.getRealNumericContext().getPrecision());
      return new SFixed(seq.numeric().sub(fixedA.getSInt(), unscaledB));
    });
  }

  @Override
  public DRes<SReal> mult(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed fixedA = (SFixed) a.out();
      SFixed fixedB = (SFixed) b.out();
      DRes<SInt> result = seq.numeric().mult(fixedA.getSInt(), fixedB.getSInt());
      DRes<SInt> truncated = scale(seq, result, -seq.getRealNumericContext().getPrecision());
      return new SFixed(truncated);
    });
  }

  @Override
  public DRes<SReal> mult(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigInteger unscaledA = unscaled(a, seq.getRealNumericContext().getPrecision());
      SFixed fixedB = (SFixed) b.out();
      DRes<SInt> result = seq.numeric().mult(unscaledA, fixedB.getSInt());
      DRes<SInt> truncated = scale(seq, result, -seq.getRealNumericContext().getPrecision());
      return new SFixed(truncated);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed fixedA = (SFixed) a.out();
      SFixed fixedB = (SFixed) b.out();
      DRes<SInt> scaledA = scale(seq, fixedA.getSInt(), seq.getRealNumericContext().getPrecision());
      DRes<SInt> result = seq.advancedNumeric().div(scaledA, fixedB.getSInt());
      return new SFixed(result);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFixed fixedA = (SFixed) a.out();
      DRes<SInt> scaledA = scale(seq, fixedA.getSInt(), seq.getRealNumericContext().getPrecision());
      BigInteger unscaledB = unscaled(b, seq.getRealNumericContext().getPrecision());
      DRes<SInt> result = seq.advancedNumeric().div(scaledA, unscaledB);
      return new SFixed(result);
    });
  }

  @Override
  public DRes<SReal> known(BigDecimal value) {
    return builder.seq(seq -> {
      DRes<SInt> input = seq.numeric().known(unscaled(value, seq.getRealNumericContext().getPrecision()));
      return new SFixed(input);
    });
  }

  @Override
  public DRes<SReal> fromSInt(DRes<SInt> value) {
    return builder.seq(seq -> {
      DRes<SInt> unscaled = scale(seq, value, seq.getRealNumericContext().getPrecision());
      return new SFixed(unscaled);
    });
  }

  @Override
  public DRes<SReal> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      DRes<SInt> input = seq.numeric().input(unscaled(value, seq.getRealNumericContext().getPrecision()), 
          inputParty);
      return new SFixed(input);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> x) {
    return builder.seq(seq -> {
      SFixed fixedX = (SFixed) x.out();
      DRes<SInt> unscaled = fixedX.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled);
      return unscaledOpen;
    }).seq((seq, unscaledOpen) -> {
      BigDecimal open = scaled(builder.getBasicNumericContext().getFieldDefinition(), unscaledOpen,
          seq.getRealNumericContext().getPrecision());
      return () -> open;
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> x, int outputParty) {
    return builder.seq(seq -> {
      SFixed fixedX = (SFixed) x.out();
      DRes<SInt> unscaled = fixedX.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled, outputParty);
      return unscaledOpen;
    }).seq((seq, unscaledOpen) -> {
      if (outputParty == seq.getBasicNumericContext().getMyId()) {
        BigDecimal open = scaled(builder.getBasicNumericContext().getFieldDefinition(), unscaledOpen,
            seq.getRealNumericContext().getPrecision());
        return () -> open;
      } else {
        return () -> null;
      }
    });
  }

  @Override
  public DRes<SInt> leq(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed fixedA = (SFixed) a.out();
      SFixed fixedB = (SFixed) b.out();
      return seq.comparison().compareLEQ(fixedA.getSInt(), fixedB.getSInt());
    });
  }
}
