package dk.alexandra.fresco.fixedpoint;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class DefaultFixedNumeric implements FixedNumeric {

  private final int precision;
  private final ProtocolBuilderNumeric builder;

  /**
   * Creates a FixedNumeric which creates basic numeric operations
   *
   * @param builder The protocolbuilder used to construct underlying protocols.
   * @param precision  Amount of digits after the dot.
   */
  public DefaultFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    super();
    this.precision = precision;
    this.builder = builder;
  }
  
  @Override
  public DRes<SFixed> known(BigDecimal value) {
    value = value.setScale(this.precision, RoundingMode.HALF_UP);
    DRes<SInt> input = builder.numeric().known(value.unscaledValue());
    return () ->  
    new SFixed(input);
  }

  @Override
  public DRes<SFixed> input(BigDecimal value, int inputParty) {
    value = value.setScale(this.precision, RoundingMode.HALF_UP);
    DRes<SInt> input = builder.numeric().input(value.unscaledValue(), inputParty);
    return () ->  new SFixed(input);
  }

  @Override
  public DRes<BigDecimal> open(DRes<SFixed> secretShare) {
    DRes<SInt> sint = secretShare.out().getSInt();
    DRes<BigInteger> bi = builder.numeric().open(sint);
    return () -> new BigDecimal(bi.out(), precision);
  }

  @Override
  public DRes<BigDecimal> open(DRes<SFixed> secretShare, int outputParty) {
    DRes<SInt> sint = secretShare.out().getSInt();
    DRes<BigInteger> bi = builder.numeric().open(sint, outputParty);
    return () -> {
      if (bi.out() != null) {
        return new BigDecimal(bi.out(), precision);
      } else {
        return null;
      }
    };
  }

  @Override
  public DRes<SFixed> add(BigDecimal a, DRes<SFixed> b) {
    a = a.setScale(this.precision, RoundingMode.HALF_UP);
    DRes<SInt> sint = b.out().getSInt();
    DRes<SInt> input = builder.numeric().add(a.unscaledValue(), sint);
    return () ->  new SFixed(input);
  }

  @Override
  public DRes<SFixed> add(DRes<SFixed> a, DRes<SFixed> b) {
    return () ->  
    new SFixed(builder.numeric().add(a.out().getSInt(), b.out().getSInt()));
  }

  @Override
  public DRes<SFixed> sub(DRes<SFixed> a, DRes<SFixed> b) {
    return () -> 
    new SFixed(builder.numeric().sub(a.out().getSInt(), b.out().getSInt()));
  }

  @Override
  public DRes<SFixed> sub(BigDecimal a, DRes<SFixed> b) {
    a = a.setScale(this.precision, RoundingMode.HALF_UP);
    DRes<SInt> sint = b.out().getSInt();
    DRes<SInt> input = builder.numeric().sub(a.unscaledValue(), sint);
    return () ->  new SFixed(input);
  }

  @Override
  public DRes<SFixed> sub(DRes<SFixed> a, BigDecimal b) {
    b = b.setScale(this.precision, RoundingMode.HALF_UP);
    DRes<SInt> sint = a.out().getSInt();
    DRes<SInt> input = builder.numeric().sub(sint, b.unscaledValue());
    return () ->  new SFixed(input);
  }

  @Override
  public DRes<SFixed> mult(DRes<SFixed> a, DRes<SFixed> b) {
    DRes<SInt> sintA = a.out().getSInt();
    DRes<SInt> sintB = b.out().getSInt();
    return () -> {
      DRes<SInt> input = builder.numeric().mult(sintA, sintB);
      DRes<SInt> output = builder.advancedNumeric()
          .div(input, BigInteger.TEN.pow(this.precision));
      return new SFixed(output);
    };
  }

  @Override
  public DRes<SFixed> mult(BigDecimal a, DRes<SFixed> b) {
    a = a.setScale(this.precision, RoundingMode.HALF_UP);
    DRes<SInt> sintB = b.out().getSInt();
    DRes<SInt> input = builder.numeric().mult(a.unscaledValue(), sintB);

    return () -> {
      DRes<SInt> output = builder.advancedNumeric()
          .div(input, BigInteger.TEN.pow(this.precision));
      return new SFixed(output);
    };
  }
}
