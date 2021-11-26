package dk.alexandra.fresco.lib.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.truncations.Truncation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An implementation of the {@link FixedNumeric} ComputationDirectory based on a fixed point
 * representation of fixed numbers.
 */
public class DefaultFixedNumeric extends FixedNumeric {

  protected final ProtocolBuilderNumeric builder;
  private final Truncation truncation;

  /**
   * Creates a new fixed point based FixedNumeric ComputationDirectory
   *
   * @param builder a ProtocolBuilder for the numeric computations which will be used to implement
   *     the fixed point operations.
   */
  protected DefaultFixedNumeric(ProtocolBuilderNumeric builder, Truncation truncation) {
    Objects.requireNonNull(builder);
    this.builder = builder;
    this.truncation = truncation;
  }

  /**
   * Returns <i>N</i>, the integer used to represent 1.
   */
  private BigInteger getRepresentationOfOne() {
    return truncation.getDivisor();
  }

  /**
   * Return <i>value * N</i> rounded to the nearest integer, eg. the best
   * integer for representing the given value.
   * 
   * @param value A decimal value.
   * @return An fixed point arithmetic representation of the given value
   */
  private BigInteger unscaled(BigDecimal value) {
    return value.multiply(new BigDecimal(getRepresentationOfOne())).setScale(0, RoundingMode.HALF_UP)
        .toBigIntegerExact();
  }

  /**
   * Return the value represented by the given integer.
   *
   * @param unscaled The unscaled value
   * @return the value <i>unscaled * N<sup>-1</sup></i>.
   */
  private BigDecimal scaled(
      FieldDefinition fieldDefinition,
      BigInteger unscaled) {
    return new BigDecimal(fieldDefinition.convertToSigned(unscaled))
        .setScale(getRepresentationOfOne().bitLength(), RoundingMode.UNNECESSARY)
        .divide(new BigDecimal(getRepresentationOfOne()), RoundingMode.HALF_UP);
  }

  /**
   * Scale the given secret integer <i>n</i>. This is equivalent to multiplying <i>n</i> with
   * <i>2<sup>scale</sup></i>. Note that <i>n</i> may be negative.
   *
   * @param scope a builder used to build the required computations
   * @param n a secret integer
   * @return a DRes eventually holding the scaled value
   */
  private DRes<SInt> scale(ProtocolBuilderNumeric scope, DRes<SInt> n, boolean up) {
    if (up) {
      return scope.numeric().mult(getRepresentationOfOne(), n);
    } else {
      return truncation.truncate(n, scope);
    }
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
      BigInteger intA = unscaled(a);
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
      BigInteger scaledA = unscaled(a);
      SFixed fixedB = b.out();
      return new SFixed(seq.numeric().sub(scaledA, fixedB.getSInt()));
    });
  }

  @Override
  public DRes<SFixed> sub(DRes<SFixed> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFixed fixedA = a.out();
      BigInteger unscaledB = unscaled(b);
      return new SFixed(seq.numeric().sub(fixedA.getSInt(), unscaledB));
    });
  }

  @Override
  public DRes<SFixed> mult(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      SFixed fixedA = a.out();
      SFixed fixedB = b.out();
      DRes<SInt> result = seq.numeric().mult(fixedA.getSInt(), fixedB.getSInt());
      DRes<SInt> truncated = scale(seq, result, false);
      return new SFixed(truncated);
    });
  }

  @Override
  public DRes<SFixed> mult(BigDecimal a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      BigInteger unscaledA = unscaled(a);
      SFixed fixedB = b.out();
      DRes<SInt> result = seq.numeric().mult(unscaledA, fixedB.getSInt());
      DRes<SInt> truncated = scale(seq, result, false);
      return new SFixed(truncated);
    });
  }

  @Override
  public DRes<SFixed> div(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      SFixed fixedA = a.out();
      SFixed fixedB = b.out();
      DRes<SInt> scaledA = scale(seq, fixedA.getSInt(), true);
      DRes<SInt> result = AdvancedNumeric.using(seq).div(scaledA, fixedB.getSInt());
      return new SFixed(result);
    });
  }

  @Override
  public DRes<SFixed> div(DRes<SFixed> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFixed fixedA = a.out();
      DRes<SInt> scaledA = scale(seq, fixedA.getSInt(), true);
      BigInteger unscaledB = unscaled(b);
      DRes<SInt> result = AdvancedNumeric.using(seq).div(scaledA, unscaledB);
      return new SFixed(result);
    });
  }

  @Override
  public DRes<SFixed> known(BigDecimal value) {
    return builder.seq(seq -> {
      DRes<SInt> input = seq.numeric().known(unscaled(value));
      return new SFixed(input);
    });
  }

  @Override
  public DRes<SFixed> fromSInt(DRes<SInt> value) {
    return builder.seq(seq -> {
      DRes<SInt> unscaled = scale(seq, value, true);
      return new SFixed(unscaled);
    });
  }

  @Override
  public DRes<SFixed> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      DRes<SInt> input = seq.numeric().input(value != null ? unscaled(value) : null,
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
      BigDecimal open = scaled(builder.getBasicNumericContext().getFieldDefinition(), unscaledOpen);
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
        BigDecimal open = scaled(builder.getBasicNumericContext().getFieldDefinition(), unscaledOpen);
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
