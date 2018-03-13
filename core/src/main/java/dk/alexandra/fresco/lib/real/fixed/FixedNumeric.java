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

public class FixedNumeric implements RealNumeric {

  private final ProtocolBuilderNumeric builder;
  private final BigInteger BASE = BigInteger.valueOf(2);
  private final int defaultPrecision;
  private final int maxPrecision;

  public FixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.defaultPrecision = precision;

    // We reserve as many bits the integer part as for the fractional part.
    this.maxPrecision = builder.getBasicNumericContext().getMaxBitLength() / 4;
    if (defaultPrecision > maxPrecision) {
      throw new IllegalArgumentException(
          "The precision was chosen too large for a product of two numbers to be representable "
              + "in this numeric context. You cannot choose a precision larger than "
              + maxPrecision + ".");
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
   * @return
   */
  private BigDecimal scaled(BigInteger unscaled, int scale) {
    return new BigDecimal(unscaled).setScale(scale).divide(new BigDecimal(BASE.pow(scale)),
        RoundingMode.HALF_UP);
  }

  /**
   * Scale the given secret integer <i>n</i>. This is equivalent to multiplying <i>n</i> with
   * <i>2<sup>scale</sup></i>. Note that <i>n</i> may be negative.
   * 
   * @param scope
   * @param n
   * @param scale
   * @return
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
      SFixed aFloat = (SFixed) a.out();
      SFixed bFloat = (SFixed) b.out();
      int precision = Math.max(aFloat.getPrecision(), (bFloat.getPrecision()));
      DRes<SInt> aUnscaled = unscaled(seq, aFloat, precision);
      DRes<SInt> bUnscaled = unscaled(seq, bFloat, precision);
      return new SFixed(seq.numeric().add(aUnscaled, bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> add(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed bFloat = (SFixed) b.out();
      int precision = Math.max(defaultPrecision, bFloat.getPrecision());
      BigInteger aInt = unscaled(a, precision);
      DRes<SInt> bUnscaled = unscaled(seq, (SFixed) b.out(), precision);
      return new SFixed(seq.numeric().add(aInt, bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed aFloat = (SFixed) a.out();
      SFixed bFloat = (SFixed) b.out();
      int precision = Math.max(aFloat.getPrecision(), bFloat.getPrecision());
      DRes<SInt> aUncaled = unscaled(seq, aFloat, precision);
      DRes<SInt> bUncaled = unscaled(seq, bFloat, precision);
      return new SFixed(seq.numeric().sub(aUncaled, bUncaled), precision);
    });
  }

  @Override
  public DRes<SReal> sub(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed bFloat = (SFixed) b.out();
      int precision = Math.max(defaultPrecision, bFloat.getPrecision());
      BigInteger aScaled = unscaled(a, precision);
      DRes<SInt> bUnscaled = unscaled(seq, bFloat, precision);
      return new SFixed(seq.numeric().sub(aScaled, bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFixed aFloat = (SFixed) a.out();
      int precision = Math.max(defaultPrecision, aFloat.getPrecision());
      DRes<SInt> aUnscaled = unscaled(seq, aFloat, precision);
      BigInteger bUnscaled = unscaled(b, precision);
      return new SFixed(seq.numeric().sub(aUnscaled, bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> mult(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed aFloat = (SFixed) a.out();
      SFixed bFloat = (SFixed) b.out();
      int precision = aFloat.getPrecision() + bFloat.getPrecision();
      DRes<SInt> unscaled = seq.numeric().mult(aFloat.getSInt(), bFloat.getSInt());

      if (precision > maxPrecision) {
        /*
         * We allow for 'pseudo-floating-point' numbers where the precision may increase after each
         * multiplications and we only truncate when reaching an upper bound for the precision.
         *
         * For performance reasons, we use the Truncate algorithm instead of RightShift when
         * truncating numbers, so every time this is done to the SInt used to represent a fixed
         * number, eg. after multiplication, there is a propability (p ~ 0.5) that the result will
         * be one larger than the expected value which will make the corresponding fixed point
         * number 2^-n larger than the expected value.
         */ unscaled = scale(seq, unscaled, defaultPrecision - precision);
        precision = defaultPrecision;
      }
      return new SFixed(unscaled, precision);
    });
  }

  @Override
  public DRes<SReal> mult(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed bFloat = (SFixed) b.out();
      int precision = defaultPrecision + bFloat.getPrecision();
      BigInteger aUnscaled = unscaled(a, defaultPrecision);
      DRes<SInt> unscaled = seq.numeric().mult(aUnscaled, bFloat.getSInt());
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
      SFixed aFloat = (SFixed) a.out();
      SFixed bFloat = (SFixed) b.out();

      DRes<SInt> aInt = unscaled(seq, aFloat, 2 * defaultPrecision);
      DRes<SInt> bInt = unscaled(seq, bFloat, defaultPrecision);

      DRes<SInt> scaled = seq.advancedNumeric().div(aInt, bInt);
      return new SFixed(scaled, defaultPrecision);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFixed aFloat = (SFixed) a.out();

      DRes<SInt> aInt = unscaled(seq, aFloat, 2 * defaultPrecision);
      BigInteger bUnscaled = unscaled(b, defaultPrecision);

      DRes<SInt> scaledResult = seq.advancedNumeric().div(aInt, bUnscaled);
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
      SFixed xFloat = (SFixed) x.out();
      DRes<SInt> unscaled = xFloat.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled);
      int precision = xFloat.getPrecision();
      return () -> scaled(unscaledOpen.out(), precision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> x, int outputParty) {
    return builder.seq(seq -> {
      SFixed xFloat = (SFixed) x.out();
      DRes<SInt> unscaled = xFloat.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled, outputParty);
      int precision = xFloat.getPrecision();
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
      SFixed aFloat = (SFixed) a.out();
      SFixed bFloat = (SFixed) b.out();

      int scale = Math.max(aFloat.getPrecision(), (bFloat.getPrecision()));
      DRes<SInt> aUnscaled = unscaled(seq, aFloat, scale);
      DRes<SInt> bUnscaled = unscaled(seq, bFloat, scale);

      return seq.comparison().compareLEQ(aUnscaled, bUnscaled);
    });
  }
}
