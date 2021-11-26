package dk.alexandra.fresco.suite.crt.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.DefaultFixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.lib.fixed.truncations.Truncation;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.protocols.BitLengthProtocol;
import dk.alexandra.fresco.suite.crt.protocols.LEQProtocol;
import dk.alexandra.fresco.suite.crt.protocols.SecretSharedDivisionProtocol;
import dk.alexandra.fresco.suite.crt.protocols.Truncp;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class CRTFixedNumeric extends DefaultFixedNumeric {

  private final BigInteger p;

  public CRTFixedNumeric(ProtocolBuilderNumeric builder) {
    super(builder, new Truncation() {

      @Override
      public BigInteger getDivisor() {
        return ((CRTNumericContext) builder.getBasicNumericContext()).getP();
      }

      @Override
      public DRes<SInt> truncate(DRes<SInt> value, ProtocolBuilderNumeric scope) {
        return new Truncp(value).buildComputation(scope);
      }
    });
    this.p = ((CRTNumericContext) builder.getBasicNumericContext()).getP();
  }

  @Override
  public DRes<SInt> leq(DRes<SFixed> x, DRes<SFixed> y) {
    return builder.seq(new LEQProtocol(x.out().getSInt(), y.out().getSInt()));
  }

  @Override
  public DRes<SFixed> div(DRes<SFixed> a, BigDecimal b) {
    return builder.seq(seq -> {
      BigInteger D = new BigDecimal(p).divide(b, MathContext.DECIMAL128).toBigInteger();
      DRes<SInt> xp = seq.numeric().mult(D, a.out().getSInt());
      xp = seq.seq(new Truncp(xp));
      return new SFixed(xp);
    });
  }

  @Override
  public DRes<SFixed> div(DRes<SFixed> a, DRes<SFixed> b) {
    return builder.seq(new SecretSharedDivisionProtocol(a, b));
  }


}
