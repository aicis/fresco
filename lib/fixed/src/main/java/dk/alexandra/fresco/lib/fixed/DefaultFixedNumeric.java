package dk.alexandra.fresco.lib.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An implementation of the {@link FixedNumeric} ComputationDirectory based on a fixed point
 * representation of fixed numbers.
 */
public class DefaultFixedNumeric implements FixedNumeric {

  private static final BigInteger TWO = BigInteger.valueOf(2);
  private final ProtocolBuilderNumeric builder;

  /**
   * Creates a new fixed point based FixedNumeric ComputationDirectory
   *
   * @param builder a ProtocolBuilder for the numeric computations which will be used to implement
   *     the fixed point operations.
   */
  DefaultFixedNumeric(ProtocolBuilderNumeric builder) {
    Objects.requireNonNull(builder);
    this.builder = builder;
  }

  /**
   * Return <i>value * 2<sup>precision</sup></i> rounded to the nearest integer, eg. the best
   * integer for representing a given value as a fixed point in base two with the given precision.
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
  private static DRes<SInt> scale(ProtocolBuilderNumeric scope, DRes<SInt> n, int scale) {
    if (scale >= 0) {
      n = scope.numeric().mult(BigInteger.ONE.shiftLeft(scale), n);
    } else {
      n = AdvancedNumeric.using(scope).truncate(n, -scale);
    }
    return n;
  }

  @Override
  public DRes<SFixed> add(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      SFixed fixedA = a.out();
      SFixed fixedB = b.out();
      return new SFixed(seq.numeric().add(fixedA.getSInt(), fixedB.getSInt()));
    });
  }

  @Override
  public DRes<SFixed> add(BigDecimal a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      BigInteger intA = unscaled(a, seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      SFixed fixedB = b.out();
      return new SFixed(seq.numeric().add(intA, fixedB.getSInt()));
    });
  }

  @Override
  public DRes<SFixed> sub(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      SFixed fixedA = a.out();
      SFixed fixedB = b.out();
      return new SFixed(seq.numeric().sub(fixedA.getSInt(), fixedB.getSInt()));
    });
  }

  @Override
  public DRes<SFixed> sub(BigDecimal a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      BigInteger scaledA = unscaled(a, seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      SFixed fixedB = b.out();
      return new SFixed(seq.numeric().sub(scaledA, fixedB.getSInt()));
    });
  }

  @Override
  public DRes<SFixed> sub(DRes<SFixed> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFixed fixedA = a.out();
      BigInteger unscaledB = unscaled(b, seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      return new SFixed(seq.numeric().sub(fixedA.getSInt(), unscaledB));
    });
  }

  @Override
  public DRes<SFixed> mult(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      SFixed fixedA = a.out();
      SFixed fixedB = b.out();
      DRes<SInt> result = seq.numeric().mult(fixedA.getSInt(), fixedB.getSInt());
      DRes<SInt> truncated = scale(seq, result, -seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      return new SFixed(truncated);
    });
  }

  @Override
  public DRes<SFixed> mult(BigDecimal a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      BigInteger unscaledA = unscaled(a, seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      SFixed fixedB = b.out();
      DRes<SInt> result = seq.numeric().mult(unscaledA, fixedB.getSInt());
      DRes<SInt> truncated = scale(seq, result, -seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      return new SFixed(truncated);
    });
  }

  @Override
  public DRes<SFixed> div(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      SFixed fixedA = a.out();
      SFixed fixedB = b.out();
      DRes<SInt> scaledA = scale(seq, fixedA.getSInt(), seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      DRes<SInt> result = AdvancedNumeric.using(seq).div(scaledA, fixedB.getSInt());
      return new SFixed(result);
    });
  }

  @Override
  public DRes<SFixed> div(DRes<SFixed> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFixed fixedA = a.out();
      DRes<SInt> scaledA = scale(seq, fixedA.getSInt(), seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      BigInteger unscaledB = unscaled(b, seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      DRes<SInt> result = AdvancedNumeric.using(seq).div(scaledA, unscaledB);
      return new SFixed(result);
    });
  }

  @Override
  public DRes<SFixed> known(BigDecimal value) {
    return builder.seq(seq -> {
      DRes<SInt> input = seq.numeric().known(unscaled(value, seq.getBasicNumericContext().getDefaultFixedPointPrecision()));
      return new SFixed(input);
    });
  }

  @Override
  public DRes<SFixed> fromSInt(DRes<SInt> value) {
    return builder.seq(seq -> {
      DRes<SInt> unscaled = scale(seq, value, seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      return new SFixed(unscaled);
    });
  }

  @Override
  public DRes<SFixed> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      DRes<SInt> input = seq.numeric().input(unscaled(value, seq.getBasicNumericContext().getDefaultFixedPointPrecision()),
          inputParty);
      return new SFixed(input);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SFixed> x) {
    return builder.seq(seq -> {
      SFixed fixedX = x.out();
      DRes<SInt> unscaled = fixedX.getSInt();
      return seq.numeric().open(unscaled);
    }).seq((seq, unscaledOpen) -> {
      BigDecimal open = scaled(builder.getBasicNumericContext().getFieldDefinition(), unscaledOpen,
          seq.getBasicNumericContext().getDefaultFixedPointPrecision());
      return () -> open;
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SFixed> x, int outputParty) {
    return builder.seq(seq -> {
      SFixed fixedX = x.out();
      DRes<SInt> unscaled = fixedX.getSInt();
      return seq.numeric().open(unscaled, outputParty);
    }).seq((seq, unscaledOpen) -> {
      if (outputParty == seq.getBasicNumericContext().getMyId()) {
        BigDecimal open = scaled(builder.getBasicNumericContext().getFieldDefinition(), unscaledOpen,
            seq.getBasicNumericContext().getDefaultFixedPointPrecision());
        return () -> open;
      } else {
        return () -> null;
      }
    });
  }

  @Override
  public DRes<SInt> leq(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      SFixed fixedA = a.out();
      SFixed fixedB = b.out();
      return Comparison.using(seq).compareLEQ(fixedA.getSInt(), fixedB.getSInt());
    });
  }
}
