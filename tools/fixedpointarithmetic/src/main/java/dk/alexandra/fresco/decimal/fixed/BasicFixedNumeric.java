package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.decimal.fixed.utils.Truncate;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class BasicFixedNumeric implements BasicRealNumeric {

  private final ProtocolBuilderNumeric builder;
  private final BigInteger BASE = BigInteger.valueOf(2);
  private final int defaultPrecision;
  private final int maxPrecision;


  public BasicFixedNumeric(ProtocolBuilderNumeric builder, int defaultPrecision, int maxPrecision) {
    this.builder = builder;
    this.defaultPrecision = defaultPrecision;
    this.maxPrecision = maxPrecision;
  }

  public BasicFixedNumeric(ProtocolBuilderNumeric builder) {
    this(builder, 16, 48);
  }

  /**
   * Return the unscaled value of a fixed point representation of the given public <i>value</i> with
   * the given scale.
   * 
   * @param value
   * @param scale
   * @return
   */
  private BigInteger unscaled(BigDecimal value, int scale) {
    return value.multiply(new BigDecimal(BASE.pow(scale))).toBigInteger();
  }

  /**
   * Returns the unscaled value og the given secret <i>value</i> with the given scale.
   * 
   * @param scope
   * @param value
   * @param scale
   * @return
   */
  private DRes<SInt> unscaled(ProtocolBuilderNumeric scope, SFixed value, int scale) {
    return scale(scope, value.getSInt(), scale - value.getScale());
  }

  /**
   * Return the value represented by the fixed point representation <i>unscaled *
   * 2<sup>-scale</sup></i>.
   * 
   * @param unscaled
   * @param scale
   * @return
   */
  private BigDecimal scaled(BigInteger unscaled, int scale) {
    return new BigDecimal(unscaled).setScale(20).divide(new BigDecimal(BASE.pow(scale)),
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
  protected DRes<SInt> scale(ProtocolBuilderNumeric scope, DRes<SInt> n, int scale) {
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
      int precision = Math.max(aFloat.getScale(), (bFloat.getScale()));
      DRes<SInt> aUnscaled = unscaled(seq, aFloat, precision);
      DRes<SInt> bUnscaled = unscaled(seq, bFloat, precision);
      return new SFixed(seq.numeric().add(aUnscaled, bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> add(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed bFloat = (SFixed) b.out();
      int precision = Math.max(defaultPrecision, bFloat.getScale());
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
      int precision = Math.max(aFloat.getScale(), bFloat.getScale());
      DRes<SInt> aUncaled = unscaled(seq, aFloat, precision);
      DRes<SInt> bUncaled = unscaled(seq, bFloat, precision);
      return new SFixed(seq.numeric().sub(aUncaled, bUncaled), precision);
    });
  }

  @Override
  public DRes<SReal> sub(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed bFloat = (SFixed) b.out();
      int precision = Math.max(defaultPrecision, bFloat.getScale());
      BigInteger aScaled = unscaled(a, precision);
      DRes<SInt> bUnscaled = unscaled(seq, bFloat, precision);
      return new SFixed(seq.numeric().sub(aScaled, bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFixed aFloat = (SFixed) a.out();
      int precision = Math.max(defaultPrecision, aFloat.getScale());
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
      int precision = aFloat.getScale() + bFloat.getScale();
      DRes<SInt> unscaled = seq.numeric().mult(aFloat.getSInt(), bFloat.getSInt());
      if (precision > maxPrecision) {
        unscaled = scale(seq, unscaled, defaultPrecision - precision);
        precision = defaultPrecision;
      }
      return new SFixed(unscaled, precision);
    });
  }

  @Override
  public DRes<SReal> mult(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFixed bFloat = (SFixed) b.out();
      int precision = defaultPrecision + bFloat.getScale();
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
      int precision = defaultPrecision;
      DRes<SInt> input = seq.numeric().known(unscaled(value, defaultPrecision));
      return new SFixed(input, precision);
    });
  }

  @Override
  public DRes<SReal> fromSInt(DRes<SInt> value) {
    return builder.seq(seq -> new SFixed(value.out(), 0));
  }

  @Override
  public DRes<SReal> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      int precision = defaultPrecision;
      DRes<SInt> input = seq.numeric().input(unscaled(value, defaultPrecision), inputParty);
      return new SFixed(input, precision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> x) {
    return builder.seq(seq -> {
      SFixed xFloat = (SFixed) x.out();
      DRes<SInt> unscaled = xFloat.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled);
      int precision = xFloat.getScale();
      return () -> scaled(unscaledOpen.out(), precision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> x, int outputParty) {
    return builder.seq(seq -> {
      SFixed xFloat = (SFixed) x.out();
      DRes<SInt> unscaled = xFloat.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled, outputParty);
      int precision = xFloat.getScale();
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

      int scale = Math.max(aFloat.getScale(), (bFloat.getScale()));
      DRes<SInt> aUnscaled = unscaled(seq, aFloat, scale);
      DRes<SInt> bUnscaled = unscaled(seq, bFloat, scale);

      return seq.comparison().compareLEQ(aUnscaled, bUnscaled);
    });
  }
}
