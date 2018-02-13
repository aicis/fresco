package dk.alexandra.fresco.fixedpoint;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

public class SIntWrapperFixedNumeric implements BasicFixedNumeric {

  private final int precision;
  private final ProtocolBuilderNumeric builder;

  /**
   * Creates a FixedNumeric which creates basic numeric operations
   *
   * @param builder The protocolbuilder used to construct underlying protocols.
   * @param precision Amount of digits after the dot.
   */
  public SIntWrapperFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    // super();
    this.precision = precision;
    this.builder = builder;
  }

  @Override
  public DRes<SFixed> known(BigDecimal value) {
    return builder.seq(seq -> {
      BigDecimal scaled = value.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> input = seq.numeric().known(scaled.unscaledValue());
      return new SFixedSIntWrapper(input);
    });
  }

  @Override
  public DRes<SFixed> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      BigDecimal scaled = value.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> input = seq.numeric().input(scaled.unscaledValue(), inputParty);
      return new SFixedSIntWrapper(input);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SFixed> secretShare) {
    return builder.seq(seq -> {
      DRes<SInt> sint = ((SFixedSIntWrapper) secretShare.out()).getSInt();
      DRes<BigInteger> bi = seq.numeric().open(sint);
      return () -> new BigDecimal(bi.out(), precision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SFixed> secretShare, int outputParty) {
    return builder.seq(seq -> {
      DRes<SInt> sint = ((SFixedSIntWrapper) secretShare.out()).getSInt();
      DRes<BigInteger> bi = seq.numeric().open(sint, outputParty);
      return () -> {
        if (bi.out() != null) {
          return new BigDecimal(bi.out(), precision);
        } else {
          return null;
        }
      };
    });
  }

  @Override
  public DRes<SFixed> add(BigDecimal a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      BigDecimal aScaled = a.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> sint = ((SFixedSIntWrapper) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().add(aScaled.unscaledValue(), sint);
      return new SFixedSIntWrapper(input);
    });
  }

  @Override
  public DRes<SFixed> add(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      DRes<SInt> add = seq.numeric().add(((SFixedSIntWrapper) a.out()).getSInt(),
          ((SFixedSIntWrapper) b.out()).getSInt());
      return new SFixedSIntWrapper(add);
    });
  }

  @Override
  public DRes<SFixed> sub(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      DRes<SInt> sub = seq.numeric().sub(((SFixedSIntWrapper) a.out()).getSInt(),
          ((SFixedSIntWrapper) b.out()).getSInt());
      return new SFixedSIntWrapper(sub);
    });
  }

  @Override
  public DRes<SFixed> sub(BigDecimal a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      BigDecimal aScaled = a.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> sint = ((SFixedSIntWrapper) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().sub(aScaled.unscaledValue(), sint);
      return new SFixedSIntWrapper(input);
    });
  }

  @Override
  public DRes<SFixed> sub(DRes<SFixed> a, BigDecimal b) {
    return builder.seq(seq -> {
      BigDecimal bScaled = b.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> sint = ((SFixedSIntWrapper) a.out()).getSInt();
      DRes<SInt> input = seq.numeric().sub(sint, bScaled.unscaledValue());
      return new SFixedSIntWrapper(input);
    });
  }

  @Override
  public DRes<SFixed> mult(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      DRes<SInt> sintA = ((SFixedSIntWrapper) a.out()).getSInt();
      DRes<SInt> sintB = ((SFixedSIntWrapper) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().mult(sintA, sintB);
      DRes<SInt> output = seq.advancedNumeric().div(input, BigInteger.TEN.pow(this.precision));
      return new SFixedSIntWrapper(output);
    });
  }

  @Override
  public DRes<SFixed> mult(BigDecimal a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      BigDecimal aScaled = a.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> sintB = ((SFixedSIntWrapper) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().mult(aScaled.unscaledValue(), sintB);
      DRes<SInt> output = seq.advancedNumeric().div(input, BigInteger.TEN.pow(this.precision));
      return new SFixedSIntWrapper(output);
    });
  }

  @Override
  public DRes<SFixed> div(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(seq -> {
      DRes<SInt> sintA = ((SFixedSIntWrapper) a.out()).getSInt();
      DRes<SInt> sintB = ((SFixedSIntWrapper) b.out()).getSInt();
      DRes<SInt> scaledA = seq.numeric().mult(BigInteger.TEN.pow(precision), sintA);
      DRes<SInt> input = seq.advancedNumeric().div(scaledA, sintB);
      return new SFixedSIntWrapper(input);
    });
  }

  @Override
  public DRes<SFixed> div(DRes<SFixed> a, BigDecimal b) {
    return builder.seq(seq -> {
      DRes<SInt> sintA = ((SFixedSIntWrapper) a.out()).getSInt();
      BigDecimal bScaled = b.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> input = seq.advancedNumeric().div(
          seq.numeric().mult(BigInteger.TEN.pow(this.precision), sintA), bScaled.unscaledValue());
      return new SFixedSIntWrapper(input);
    });
  }

  @Override
  public DRes<SFixed> fromSInt(DRes<SInt> value) {
    DRes<SInt> scaled = builder.numeric().mult(BigInteger.TEN.pow(precision), value);
    return new SFixedSIntWrapper(scaled);
  }

}
