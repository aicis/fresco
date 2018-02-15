package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class BasicFixedNumeric implements BasicRealNumeric {

  private final int precision;
  private final ProtocolBuilderNumeric builder;

  /**
   * Creates a FixedNumeric which creates basic numeric operations
   *
   * @param builder The protocolbuilder used to construct underlying protocols.
   * @param precision Amount of digits after the dot.
   */
  public BasicFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.precision = precision;
  }

  @Override
  public DRes<SReal> known(BigDecimal value) {
    return builder.seq(seq -> {
      BigDecimal scaled = value.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> input = seq.numeric().known(scaled.unscaledValue());
      return new SFixed(input);
    });
  }

  @Override
  public DRes<SReal> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      BigDecimal scaled = value.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> input = seq.numeric().input(scaled.unscaledValue(), inputParty);
      return new SFixed(input);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> secretShare) {
    return builder.seq(seq -> {
      DRes<SInt> sint = ((SFixed) secretShare.out()).getSInt();
      DRes<BigInteger> bi = seq.numeric().open(sint);
      return () -> new BigDecimal(bi.out(), precision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> secretShare, int outputParty) {
    return builder.seq(seq -> {
      DRes<SInt> sint = ((SFixed) secretShare.out()).getSInt();
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
  public DRes<SReal> add(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigDecimal aScaled = a.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> sint = ((SFixed) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().add(aScaled.unscaledValue(), sint);
      return new SFixed(input);
    });
  }

  @Override
  public DRes<SReal> add(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      DRes<SInt> add =
          seq.numeric().add(((SFixed) a.out()).getSInt(), ((SFixed) b.out()).getSInt());
      return new SFixed(add);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      DRes<SInt> sub =
          seq.numeric().sub(((SFixed) a.out()).getSInt(), ((SFixed) b.out()).getSInt());
      return new SFixed(sub);
    });
  }

  @Override
  public DRes<SReal> sub(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigDecimal aScaled = a.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> sint = ((SFixed) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().sub(aScaled.unscaledValue(), sint);
      return new SFixed(input);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      BigDecimal bScaled = b.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> sint = ((SFixed) a.out()).getSInt();
      DRes<SInt> input = seq.numeric().sub(sint, bScaled.unscaledValue());
      return new SFixed(input);
    });
  }

  @Override
  public DRes<SReal> mult(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      DRes<SInt> sintA = ((SFixed) a.out()).getSInt();
      DRes<SInt> sintB = ((SFixed) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().mult(sintA, sintB);
      DRes<SInt> output = seq.advancedNumeric().div(input, BigInteger.TEN.pow(this.precision));
      return new SFixed(output);
    });
  }

  @Override
  public DRes<SReal> mult(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigDecimal aScaled = a.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> sintB = ((SFixed) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().mult(aScaled.unscaledValue(), sintB);
      DRes<SInt> output = seq.advancedNumeric().div(input, BigInteger.TEN.pow(this.precision));
      return new SFixed(output);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      DRes<SInt> sintA = ((SFixed) a.out()).getSInt();
      DRes<SInt> sintB = ((SFixed) b.out()).getSInt();
      DRes<SInt> scaledA = seq.numeric().mult(BigInteger.TEN.pow(precision), sintA);
      DRes<SInt> input = seq.advancedNumeric().div(scaledA, sintB);
      return new SFixed(input);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      DRes<SInt> sintA = ((SFixed) a.out()).getSInt();
      BigDecimal bScaled = b.setScale(this.precision, RoundingMode.DOWN);
      DRes<SInt> input = seq.advancedNumeric().div(
          seq.numeric().mult(BigInteger.TEN.pow(this.precision), sintA), bScaled.unscaledValue());
      return new SFixed(input);
    });
  }

  @Override
  public DRes<SReal> fromSInt(DRes<SInt> value) {
    DRes<SInt> scaled = builder.numeric().mult(BigInteger.TEN.pow(precision), value);
    return new SFixed(scaled);
  }


}
