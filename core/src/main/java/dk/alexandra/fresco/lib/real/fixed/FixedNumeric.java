package dk.alexandra.fresco.lib.real.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
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

  private static final BigInteger BASE = BigInteger.valueOf(2);
  private final int defaultPrecision;
  private final int maxPrecision;
  private final ProtocolBuilderNumeric builder;

  /**
   * Creates a new fixed point based RealNumeric ComputationDirectory
   *
   * @param builder a ProtocolBuilder for the numeric computations which will be used to implement
   *        the fixed point operations.
   * @param precision the precision used for the fixed point numbers. The precision must be in the
   *        range <i>0 ... <code>builder.getMaxBitLength</code> / 4</i>.
   */
  public FixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.defaultPrecision = precision;
    /*
     * We reserve as many bits the integer part as for the fractional part and to be able to
     * represent products, we need to be able to hold twice that under the max bit length.
     */
    Objects.requireNonNull(builder);
    this.maxPrecision = builder.getBasicNumericContext().getMaxBitLength() / 4;
    if (defaultPrecision < 0 || defaultPrecision > maxPrecision) {
      throw new IllegalArgumentException(
          "Precision must be in the range 0 ... " + maxPrecision + " but was " + defaultPrecision);
    }
  }

  public FixedNumeric(ProtocolBuilderNumeric builder) {
    this(builder, builder.getRealNumericContext().getPrecision());
  }

  private BigInteger unscaled(BigDecimal value, int scale) {
    return value.multiply(new BigDecimal(BASE.pow(scale))).setScale(0, RoundingMode.HALF_UP)
        .toBigIntegerExact();
  }

  private DRes<SInt> unscaled(ProtocolBuilderNumeric scope, SFixed value, int scale) {
    return scale(scope, value.getSInt(), scale - value.getPrecision());
  }

  /**
   * Return the value represented by the fixed point representation <i>unscaled *
   * 2<sup>-scale</sup></i>.
   *
   * @param unscaled The unscaled value
   * @param scale The scale
   * @return the value <i>unscaled * 2<sup>-scale</sup></i>.
   */
  private BigDecimal scaled(BigInteger unscaled, int scale) {
    return new BigDecimal(unscaled).setScale(scale).divide(new BigDecimal(BASE.pow(scale)),
        RoundingMode.HALF_UP);
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
      SFixed floatA = (SFixed) a.out();
      SFixed floatB = (SFixed) b.out();
      int precision = Math.max(floatA.getPrecision(), (floatB.getPrecision()));
      DRes<SInt> unscaledA = unscaled(seq, floatA, precision);
      DRes<SInt> unscaledB = unscaled(seq, floatB, precision);
      return new SFixed(seq.numeric().add(unscaledA, unscaledB), precision);
    });
  }

  @Override
  public DRes<SReal> add(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed floatB = (SFixed) b.out();
      int precision = Math.max(defaultPrecision, floatB.getPrecision());
      BigInteger intA = unscaled(a, precision);
      DRes<SInt> unscaledB = unscaled(seq, (SFixed) b.out(), precision);
      return new SFixed(seq.numeric().add(intA, unscaledB), precision);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed floatA = (SFixed) a.out();
      SFixed floatB = (SFixed) b.out();
      int precision = Math.max(floatA.getPrecision(), floatB.getPrecision());
      DRes<SInt> unscaledA = unscaled(seq, floatA, precision);
      DRes<SInt> unscaledB = unscaled(seq, floatB, precision);
      return new SFixed(seq.numeric().sub(unscaledA, unscaledB), precision);
    });
  }

  @Override
  public DRes<SReal> sub(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed floatB = (SFixed) b.out();
      int precision = Math.max(defaultPrecision, floatB.getPrecision());
      BigInteger scaledA = unscaled(a, precision);
      DRes<SInt> unscaledB = unscaled(seq, floatB, precision);
      return new SFixed(seq.numeric().sub(scaledA, unscaledB), precision);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFixed floatA = (SFixed) a.out();
      int precision = Math.max(defaultPrecision, floatA.getPrecision());
      DRes<SInt> unscaledA = unscaled(seq, floatA, precision);
      BigInteger unscaledB = unscaled(b, precision);
      return new SFixed(seq.numeric().sub(unscaledA, unscaledB), precision);
    });
  }

  @Override
  public DRes<SReal> mult(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed floatA = (SFixed) a.out();
      SFixed floatB = (SFixed) b.out();
      int precision = floatA.getPrecision() + floatB.getPrecision();
      DRes<SInt> unscaled = seq.numeric().mult(floatA.getSInt(), floatB.getSInt());
      if (precision > maxPrecision) {
        /*
         * We allow for 'pseudo-floating-point' numbers where the precision may increase after each
         * multiplications and we only truncate when reaching an upper bound for the precision. This
         * is motly effective when the precision was chosen small compared to the max bit length in
         * the underlying field.
         *
         * For performance reasons, we use the Truncate algorithm instead of RightShift when
         * truncating numbers, so every time this is done to the SInt used to represent a fixed
         * number, eg. after multiplication, there is a propability (p ~ 0.5) that the result will
         * be one larger than the expected value which will make the corresponding fixed point
         * number 2^-n larger than the expected value.
         */
        unscaled = scale(seq, unscaled, defaultPrecision - precision);
        precision = defaultPrecision;
      }
      return new SFixed(unscaled, precision);
    });
  }

  @Override
  public DRes<SReal> mult(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed floatB = (SFixed) b.out();
      int precision = defaultPrecision + floatB.getPrecision();
      BigInteger unscaledA = unscaled(a, defaultPrecision);
      DRes<SInt> unscaled = seq.numeric().mult(unscaledA, floatB.getSInt());
      if (precision > maxPrecision) {
        unscaled = scale(seq, unscaled, defaultPrecision - precision);
        precision = defaultPrecision;
      }
      return new SFixed(unscaled, precision);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed floatA = (SFixed) a.out();
      SFixed floatB = (SFixed) b.out();
      DRes<SInt> intA = unscaled(seq, floatA, 2 * defaultPrecision);
      DRes<SInt> intB = unscaled(seq, floatB, defaultPrecision);
      DRes<SInt> scaled = seq.advancedNumeric().div(intA, intB);
      return new SFixed(scaled, defaultPrecision);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFixed floatA = (SFixed) a.out();
      DRes<SInt> intA = unscaled(seq, floatA, 2 * defaultPrecision);
      BigInteger unscaledB = unscaled(b, defaultPrecision);
      DRes<SInt> scaledResult = seq.advancedNumeric().div(intA, unscaledB);
      return new SFixed(scaledResult, defaultPrecision);
    });
  }

  @Override
  public DRes<SReal> known(BigDecimal value) {
    return builder.seq(seq -> {
      DRes<SInt> input = seq.numeric().known(unscaled(value, defaultPrecision));
      return new SFixed(input, defaultPrecision);
    });
  }

  @Override
  public DRes<SReal> fromSInt(DRes<SInt> value) {
    return builder.seq(seq -> new SFixed(value.out(), 0));
  }

  @Override
  public DRes<SReal> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      DRes<SInt> input = seq.numeric().input(unscaled(value, defaultPrecision), inputParty);
      return new SFixed(input, defaultPrecision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> x) {
    return builder.seq(seq -> {
      SFixed floatX = (SFixed) x.out();
      DRes<SInt> unscaled = floatX.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled);
      int precision = floatX.getPrecision();
      return () -> scaled(unscaledOpen.out(), precision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> x, int outputParty) {
    return builder.seq(seq -> {
      SFixed floatX = (SFixed) x.out();
      DRes<SInt> unscaled = floatX.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled, outputParty);
      int precision = floatX.getPrecision();
      return () -> {
        if (unscaledOpen.out() != null) {
          return scaled(unscaledOpen.out(), precision);
        } else {
          return null;
        }
      };
    });
  }

  @Override
  public DRes<SInt> leq(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed floatA = (SFixed) a.out();
      SFixed floatB = (SFixed) b.out();
      int scale = Math.max(floatA.getPrecision(), (floatB.getPrecision()));
      DRes<SInt> unscaledA = unscaled(seq, floatA, scale);
      DRes<SInt> unscaledB = unscaled(seq, floatB, scale);
      return seq.comparison().compareLEQ(unscaledA, unscaledB);
    });
  }
}
