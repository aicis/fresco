package dk.alexandra.fresco.fixedpoint;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.math.BigDecimal;

public class FixedNumeric {

  private final int precision;
  private final ProtocolBuilderNumeric builder;
  private Numeric numeric;

  public FixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    super();
    this.precision = precision;
    this.builder = builder;
    this.numeric = builder.numeric();
  }
  
 public DRes<SFixed> add(DRes<SFixed> a, DRes<SFixed> b) {
   return () ->  
     new SFixed(numeric.add(a.out().getSInt(), b.out().getSInt()));
  }

 public DRes<SFixed> known(BigDecimal value) {
   return () ->  
   new SFixed(numeric.known(value.unscaledValue()));
 }

 public DRes<SFixed> input(BigDecimal value, int inputParty) {
   System.out.println("value: "+value+" unscaleD: "+value.unscaledValue()+" scale: "+value.scale());
   value = value.setScale(this.precision, RoundingMode.HALF_UP);
   System.out.println("after scale");
   System.out.println("value: "+value+" unscaleD: "+value.unscaledValue()+" scale: "+value.scale());
   DRes<SInt> input = numeric.input(value.unscaledValue(), inputParty);
   return () ->  new SFixed(input);
 }

 
 public DRes<BigDecimal> open(DRes<SFixed> secretShare) {
   DRes<SInt> sint = secretShare.out().getSInt();
   DRes<BigInteger> bi = numeric.open(sint);
   return () -> new BigDecimal(bi.out(), precision);
 }
 
/*
 public DRes<BigDecimal> open(DRes<SFixed> secretShare, int outputParty) {
   return numeric.open(secretShare, outputParty);
 }

 */
/*
  @Override
  public DRes<SFixed> add(BigDecimal a, DRes<SFixed> b) {
    
    SpdzAddProtocolKnownLeft spdzAddProtocolKnownLeft = new SpdzAddProtocolKnownLeft(a, b);
    return protocolBuilder.append(spdzAddProtocolKnownLeft);
  }


  @Override
  public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
    SpdzSubtractProtocol spdzSubtractProtocol = new SpdzSubtractProtocol(a, b);
    return protocolBuilder.append(spdzSubtractProtocol);
  }

  @Override
  public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
    SpdzSubtractProtocolKnownLeft spdzSubtractProtocolKnownLeft =
        new SpdzSubtractProtocolKnownLeft(a, b);
    return protocolBuilder.append(spdzSubtractProtocolKnownLeft);
  }

  @Override
  public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
    SpdzSubtractProtocolKnownRight spdzSubtractProtocolKnownRight =
        new SpdzSubtractProtocolKnownRight(a, b);
    return protocolBuilder.append(spdzSubtractProtocolKnownRight);
  }

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
