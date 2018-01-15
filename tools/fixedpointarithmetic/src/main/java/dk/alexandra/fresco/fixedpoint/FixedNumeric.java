package dk.alexandra.fresco.fixedpoint;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.math.BigDecimal;

public class FixedNumeric {

  private final int precision;
  private final ProtocolBuilderNumeric builder;
  private final Numeric numeric;

  public FixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    super();
    this.precision = precision;
    this.builder = builder;
    this.numeric = builder.numeric();
  }



  public DRes<SFixed> known(BigDecimal value) {
    value = value.setScale(this.precision, RoundingMode.HALF_UP);
    DRes<SInt> input = numeric.known(value.unscaledValue());
    return () ->  
    new SFixed(input);
  }

  public DRes<SFixed> input(BigDecimal value, int inputParty) {
    value = value.setScale(this.precision, RoundingMode.HALF_UP);
    DRes<SInt> input = numeric.input(value.unscaledValue(), inputParty);
    return () ->  new SFixed(input);
  }


  public DRes<BigDecimal> open(DRes<SFixed> secretShare) {
    DRes<SInt> sint = secretShare.out().getSInt();
    DRes<BigInteger> bi = numeric.open(sint);
    return () -> new BigDecimal(bi.out(), precision);
  }


  public DRes<BigDecimal> open(DRes<SFixed> secretShare, int outputParty) {
    DRes<SInt> sint = secretShare.out().getSInt();
    DRes<BigInteger> bi = numeric.open(sint, outputParty);
    return () -> new BigDecimal(bi.out(), precision);
  }

  public DRes<SFixed> add(BigDecimal a, DRes<SFixed> b) {
    a = a.setScale(this.precision, RoundingMode.HALF_UP);
    DRes<SInt> sint = b.out().getSInt();
    DRes<SInt> input = numeric.add(a.unscaledValue(), sint);
    return () ->  new SFixed(input);
  }

  public DRes<SFixed> add(DRes<SFixed> a, DRes<SFixed> b) {
    return () ->  
    new SFixed(numeric.add(a.out().getSInt(), b.out().getSInt()));
  }

  public DRes<SFixed> sub(DRes<SFixed> a, DRes<SFixed> b) {
    return () -> 
    new SFixed(numeric.sub(a.out().getSInt(), b.out().getSInt()));
  }

  public DRes<SFixed> sub(BigDecimal a, DRes<SFixed> b) {
    a = a.setScale(this.precision, RoundingMode.HALF_UP);
    DRes<SInt> sint = b.out().getSInt();
    DRes<SInt> input = numeric.sub(a.unscaledValue(), sint);
    return () ->  new SFixed(input);
  }

  public DRes<SFixed> sub(DRes<SFixed> a, BigDecimal b) {
    b = b.setScale(this.precision, RoundingMode.HALF_UP);
    DRes<SInt> sint = a.out().getSInt();
    DRes<SInt> input = numeric.sub(sint, b.unscaledValue());
    return () ->  new SFixed(input);
  }
/*
  @Override
  public DRes<SFixed> mult(DRes<SFixed> a, DRes<SFixed> b) {
    SpdzMultProtocol spdzMultProtocol = new SpdzMultProtocol(a, b);
    return protocolBuilder.append(spdzMultProtocol);
  }

  @Override
  public DRes<SInt> mult(BigDecimal a, DRes<SFixed> b) {
    SpdzMultProtocolKnownLeft spdzMultProtocol4 = new SpdzMultProtocolKnownLeft(a, b);
    return protocolBuilder.append(spdzMultProtocol4);

  }
*/

  
}
