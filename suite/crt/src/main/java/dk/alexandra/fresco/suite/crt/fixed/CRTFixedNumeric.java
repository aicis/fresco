package dk.alexandra.fresco.suite.crt.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.DefaultFixedNumeric;
import dk.alexandra.fresco.lib.fixed.truncations.Truncation;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.protocols.TruncOne;
import dk.alexandra.fresco.suite.crt.protocols.Truncp;
import java.math.BigInteger;

public class CRTFixedNumeric extends DefaultFixedNumeric {

  private final BigInteger p;

  public CRTFixedNumeric(ProtocolBuilderNumeric builder) {
    super(builder, new Truncation() {

      @Override
      public BigInteger getDivisor() {
        return ((CRTNumericContext) builder.getBasicNumericContext()).getLeftModulus();
      }

      /**
       * Truncation with error bound in the amount of parties
       */
      @Override
      public DRes<SInt> truncate(DRes<SInt> value, ProtocolBuilderNumeric scope) {
        return new Truncp(value).buildComputation(scope);
      }

      /**
       * Truncation with at most a single bit error
       */
      public DRes<SInt> truncateOne(DRes<SInt> value, ProtocolBuilderNumeric scope) {
        return new TruncOne(value).buildComputation(scope);
      }
    });
    this.p = ((CRTNumericContext) builder.getBasicNumericContext()).getLeftModulus();
  }

}
