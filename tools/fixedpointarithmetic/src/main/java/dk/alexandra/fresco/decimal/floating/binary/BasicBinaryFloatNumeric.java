package dk.alexandra.fresco.decimal.floating.binary;

import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.decimal.utils.BigBinary;
import dk.alexandra.fresco.decimal.utils.Truncate;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;

public class BasicBinaryFloatNumeric implements BasicRealNumeric {

  private final ProtocolBuilderNumeric builder;
  private final int defaultScale = 16;
  private final int maxScale = 48;

  /**
   * Creates a FixedNumeric which creates basic numeric operations
   *
   * @param builder The protocolbuilder used to construct underlying protocols.
   * @param precision Amount of digits after the dot.
   */
  public BasicBinaryFloatNumeric(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  int getDefaultPrecision() {
    return defaultScale;
  }

  private DRes<SInt> unscaled(ProtocolBuilderNumeric scope, SBinaryFloat current, int scale) {
    DRes<SInt> sint = current.out().getSInt();
    if (current.getScale() < scale) {
      sint = scope.numeric().mult(BigInteger.valueOf(2).pow(scale - current.getScale()), sint);
    } else if (current.getScale() > scale) {
      sint = scope.seq(new Truncate(2*maxScale, sint, current.getScale() - scale));
    }
    return sint;
  }

  @Override
  public DRes<SReal> add(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SBinaryFloat aFloat = (SBinaryFloat) a.out();
      SBinaryFloat bFloat = (SBinaryFloat) b.out();
      int precision = Math.max(aFloat.getScale(), (bFloat.getScale()));
      DRes<SInt> aUnscaled = unscaled(seq, aFloat, precision);
      DRes<SInt> bUnscaled = unscaled(seq, bFloat, precision);
      return new SBinaryFloat(seq.numeric().add(aUnscaled, bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> add(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SBinaryFloat bFloat = (SBinaryFloat) b.out();
      BigBinary aBin = new BigBinary(a, defaultScale);
      int precision = Math.max(aBin.scale(), bFloat.getScale());
      BigBinary aScaled = aBin.setScale(precision);
      DRes<SInt> bUnscaled = unscaled(seq, (SBinaryFloat) b.out(), precision);
      return new SBinaryFloat(seq.numeric().add(aScaled.unscaledValue(), bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SBinaryFloat aFloat = (SBinaryFloat) a.out();
      SBinaryFloat bFloat = (SBinaryFloat) b.out();
      int precision = Math.max(aFloat.getScale(), bFloat.getScale());
      DRes<SInt> aUncaled = unscaled(seq, aFloat, precision);
      DRes<SInt> bUncaled = unscaled(seq, bFloat, precision);
      return new SBinaryFloat(seq.numeric().sub(aUncaled, bUncaled), precision);
    });
  }

  @Override
  public DRes<SReal> sub(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SBinaryFloat bFloat = (SBinaryFloat) b.out();
      BigBinary aBin = new BigBinary(a, defaultScale);
      int precision = Math.max(aBin.scale(), bFloat.getScale());
      BigBinary aScaled = aBin.setScale(precision);
      DRes<SInt> bUnscaled = unscaled(seq, bFloat, precision);
      return new SBinaryFloat(seq.numeric().sub(aScaled.unscaledValue(), bUnscaled), precision);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      SBinaryFloat aFloat = (SBinaryFloat) a.out();
      BigBinary bBin = new BigBinary(b, defaultScale);
      int precision = Math.max(bBin.scale(), aFloat.getScale());
      DRes<SInt> aUnscaled = unscaled(seq, aFloat, precision);
      BigBinary bScaled = bBin.setScale(precision);
      return new SBinaryFloat(seq.numeric().sub(aUnscaled, bScaled.unscaledValue()), precision);
    });
  }

  @Override
  public DRes<SReal> mult(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SBinaryFloat aFloat = (SBinaryFloat) a.out();
      SBinaryFloat bFloat = (SBinaryFloat) b.out();
      int scale = aFloat.getScale() + bFloat.getScale();
      DRes<SInt> unscaled = seq.numeric().mult(aFloat.getSInt(), bFloat.getSInt());
      if (scale > maxScale) {
        unscaled = seq.seq(new Truncate(maxScale, unscaled, scale - defaultScale));
        scale = defaultScale;
      }
      return new SBinaryFloat(unscaled, scale);
    });
  }

  @Override
  public DRes<SReal> mult(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SBinaryFloat bFloat = (SBinaryFloat) b.out();
      BigBinary aBin = new BigBinary(a, defaultScale);
      int scale = aBin.scale() + bFloat.getScale();
      DRes<SInt> unscaled = seq.numeric().mult(aBin.unscaledValue(), bFloat.getSInt());
      if (scale > maxScale) {
        unscaled = seq.seq(new Truncate(maxScale, unscaled, scale - defaultScale));
        scale = defaultScale;
      }
      return new SBinaryFloat(unscaled, scale);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      SBinaryFloat aFloat = (SBinaryFloat) a.out();
      SBinaryFloat bFloat = (SBinaryFloat) b.out();
      
      DRes<SInt> aInt = unscaled(seq, aFloat, 2*defaultScale);
      DRes<SInt> bInt = unscaled(seq, bFloat, defaultScale);
      
      DRes<SInt> scaled = seq.advancedNumeric().div(aInt, bInt);
      return new SBinaryFloat(scaled, defaultScale);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      SBinaryFloat aFloat = (SBinaryFloat) a.out();
      DRes<SInt> aInt = unscaled(seq, aFloat, 2*defaultScale);
      
      BigBinary binB = new BigBinary(b, defaultScale);
      BigInteger bInt = binB.unscaledValue();
      DRes<SInt> scaled = seq.advancedNumeric().div(aInt, bInt);
      return new SBinaryFloat(scaled, defaultScale);
    });
  }

  @Override
  public DRes<SReal> known(BigDecimal value) {
    return builder.seq(seq -> {
      int precision = defaultScale;
      BigBinary binary = new BigBinary(value, defaultScale);
      DRes<SInt> input =
          seq.numeric().known(binary.unscaledValue());
      return new SBinaryFloat(input, precision);
    });
  }

  @Override
  public DRes<SReal> fromSInt(DRes<SInt> value) {
    return builder.seq(seq -> {
      return new SBinaryFloat(value.out(), 0);
    });
  }

  @Override
  public DRes<SReal> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      int precision = defaultScale;
      BigBinary binary = new BigBinary(value, defaultScale);
      DRes<SInt> input =
          seq.numeric().input(binary.unscaledValue(), inputParty);
      return new SBinaryFloat(input, precision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> x) {
    return builder.seq(seq -> {
      SBinaryFloat xFloat = (SBinaryFloat) x.out();
      DRes<SInt> unscaled = xFloat.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled);
      int precision = xFloat.getScale();
      return () -> new BigBinary(unscaledOpen.out(), precision).toBigDecimal();
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> x, int outputParty) {
    return builder.seq(seq -> {
      SBinaryFloat xFloat = (SBinaryFloat) x.out();
      DRes<SInt> unscaled = xFloat.getSInt();
      DRes<BigInteger> unscaledOpen = seq.numeric().open(unscaled, outputParty);
      int precision = xFloat.getScale();
      return () -> {
        if (unscaledOpen.out() != null) {
          return new BigBinary(unscaledOpen.out(), precision).toBigDecimal();
        } else {
          return null;
        }
      };
    });
  }

}
