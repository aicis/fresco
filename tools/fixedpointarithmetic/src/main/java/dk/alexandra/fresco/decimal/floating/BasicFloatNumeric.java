package dk.alexandra.fresco.decimal.floating;

import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class BasicFloatNumeric implements BasicRealNumeric {

  private final ProtocolBuilderNumeric builder;
  private final int minPrecision = 4;
  private final int defaultPrecision = 6;
  private final int maxPrecision = 16;

  /**
   * Creates a FixedNumeric which creates basic numeric operations
   *
   * @param builder The protocolbuilder used to construct underlying protocols.
   * @param precision Amount of digits after the dot.
   */
  public BasicFloatNumeric(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  int getDefaultPrecision() {
    return defaultPrecision;
  }

  private DRes<SInt> unscaled(ProtocolBuilderNumeric scope, SFloat current, int scale) {
    DRes<SInt> sint = current.out().getSInt();
    if (current.getScale() < scale) {
      sint = scope.numeric().mult(BigInteger.TEN.pow(scale - current.getScale()), sint);
    } else if (current.getScale() > scale) {
      sint = scope.advancedNumeric().div(sint, BigInteger.TEN.pow(current.getScale() - scale));
    }
    return sint;
  }

  @Override
  public DRes<SReal> add(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFloat aFloat = (SFloat) a.out();
      SFloat bFloat = (SFloat) b.out();
      int precision = Math.max(aFloat.getScale(), (bFloat.getScale()));
      DRes<SInt> aUnscaled = unscaled(seq, aFloat, precision);
      DRes<SInt> bUnscaled = unscaled(seq, bFloat, precision);
      return new SFloat(seq.numeric().add(aUnscaled, bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> add(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFloat bFloat = (SFloat) b.out();
      int precision = Math.max(a.scale(), bFloat.getScale());
      BigDecimal aScaled = a.setScale(precision);
      DRes<SInt> bUnscaled = unscaled(seq, (SFloat) b.out(), precision);
      return new SFloat(seq.numeric().add(aScaled.unscaledValue(), bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFloat aFloat = (SFloat) a.out();
      SFloat bFloat = (SFloat) b.out();
      int precision = Math.max(aFloat.getScale(), bFloat.getScale());
      DRes<SInt> aUncaled = unscaled(seq, aFloat, precision);
      DRes<SInt> bUncaled = unscaled(seq, bFloat, precision);
      return new SFloat(seq.numeric().sub(aUncaled, bUncaled), precision);
    });
  }

  @Override
  public DRes<SReal> sub(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFloat bFloat = (SFloat) b.out();
      int precision = Math.max(a.scale(), bFloat.getScale());
      BigDecimal aScaled = a.setScale(precision);
      DRes<SInt> bUnscaled = unscaled(seq, bFloat, precision);
      return new SFloat(seq.numeric().sub(aScaled.unscaledValue(), bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFloat aFloat = (SFloat) a.out();
      int precision = Math.max(b.scale(), aFloat.getScale());
      DRes<SInt> aUnscaled = unscaled(seq, aFloat, precision);
      BigDecimal bScaled = b.setScale(precision);
      return new SFloat(seq.numeric().sub(aUnscaled, bScaled.unscaledValue()), precision);
    });
  }

  @Override
  public DRes<SReal> mult(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFloat aFloat = (SFloat) a.out();
      SFloat bFloat = (SFloat) b.out();
      int precision = aFloat.getScale() + bFloat.getScale();
      DRes<SInt> unscaled = seq.numeric().mult(aFloat.getSInt(), bFloat.getSInt());
      if (precision > maxPrecision) {
        unscaled =
            seq.advancedNumeric().div(unscaled, BigInteger.TEN.pow(precision - defaultPrecision));
        precision = defaultPrecision;
      }
      return new SFloat(unscaled, precision);
    });
  }

  @Override
  public DRes<SReal> mult(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFloat bFloat = (SFloat) b.out();
      int precision = a.scale() + bFloat.getScale();
      DRes<SInt> unscaled = seq.numeric().mult(a.unscaledValue(), bFloat.getSInt());
      if (precision > maxPrecision) {
        unscaled =
            seq.advancedNumeric().div(unscaled, BigInteger.TEN.pow(precision - defaultPrecision));
        precision = defaultPrecision;
      }
      return new SFloat(unscaled, precision);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SFloat aFloat = (SFloat) a.out();
      SFloat bFloat = (SFloat) b.out();
      DRes<SInt> aInt = aFloat.getSInt();
      DRes<SInt> bInt = bFloat.getSInt();

      int precision = aFloat.getScale() - bFloat.getScale();
      if (precision < minPrecision) {
        if (aFloat.getScale() < 2 * minPrecision) {
          aInt = unscaled(seq, aFloat, bFloat.getScale() + defaultPrecision);
          precision = defaultPrecision;
        } else if (bFloat.getScale() > 2 * minPrecision) {
          bInt = unscaled(seq, bFloat, aFloat.getScale() - defaultPrecision);
          precision = defaultPrecision;
        }
      }
      DRes<SInt> scaled = seq.advancedNumeric().div(aInt, bInt);
      return new SFloat(scaled, precision);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      SFloat aFloat = (SFloat) a.out();
      DRes<SInt> aInt = aFloat.getSInt();
      BigInteger bInt = b.unscaledValue();
      int precision = aFloat.getScale() - b.scale();
      if (precision < minPrecision) {
        if (aFloat.getScale() < 2 * minPrecision) {
          aInt = unscaled(seq, aFloat, b.scale() + defaultPrecision);
          precision = defaultPrecision;
        } else if (b.scale() > 2 * minPrecision) {
          bInt = bInt.divide(BigInteger.TEN.pow(defaultPrecision - precision));
          precision = defaultPrecision;
        }
      }
      DRes<SInt> scaled = seq.advancedNumeric().div(aInt, b.unscaledValue());
      return new SFloat(scaled, precision);
    });
  }

  @Override
  public DRes<SReal> known(BigDecimal value) {
    return builder.seq(seq -> {
      int precision = defaultPrecision;
      DRes<SInt> input =
          seq.numeric().known(value.setScale(precision, RoundingMode.DOWN).unscaledValue());
      return new SFloat(input, precision);
    });
  }

  @Override
  public DRes<SReal> fromSInt(DRes<SInt> value) {
    return builder.seq(seq -> {
      return new SFloat(value.out(), 0);
    });
  }

  @Override
  public DRes<SReal> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      int precision = defaultPrecision;
      DRes<SInt> input = seq.numeric()
          .input(value.setScale(precision, RoundingMode.DOWN).unscaledValue(), inputParty);
      return new SFloat(input, precision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> x) {
    return builder.seq(seq -> {
      SFloat xFloat = (SFloat) x.out();
      DRes<SInt> unscaled = xFloat.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled);
      int precision = xFloat.getScale();
      return () -> new BigDecimal(unscaledOpen.out(), precision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> x, int outputParty) {
    return builder.seq(seq -> {
      SFloat xFloat = (SFloat) x.out();
      DRes<SInt> unscaled = xFloat.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled, outputParty);
      int precision = xFloat.getScale();
      return () -> {
        if (unscaledOpen.out() != null) {
          return new BigDecimal(unscaledOpen.out(), precision);
        } else {
          return null;
        }
      };
    });
  }

}
