package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.DefaultComparison;
import dk.alexandra.fresco.lib.common.math.DefaultAdvancedNumeric;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.real.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.real.fixed.utils.MultiplyWithSInt;
import java.util.List;

/**
 * Compute 2 to a secret integer power <i>n</i> which may be negative. The absolute value
 * <i>abs(n)</i> should be smaller than the fixed point precision.
 */
public class TwoPower implements Computation<SReal, ProtocolBuilderNumeric> {

  public final DRes<SInt> exponent;
  
  public TwoPower(DRes<SInt> exponent) {
    this.exponent = exponent;
  }
  
  private static int log2(double x) {
    return (int) Math.floor(Math.log(x) / Math.log(2));
  }
  
  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    
    return builder.seq(r1 -> {
      int f = builder.getBasicNumericContext().getPrecision();
      int l = log2(f);
      
      // Sign bit (0 or 1)
      DRes<SInt> b = new DefaultComparison(r1).compareLEQ(exponent, r1.numeric().known(0));
      
      // Sign (-1 or 1)
      DRes<SInt> s = r1.numeric().add(1, r1.numeric().mult(-2, b));
      
      DRes<SInt> abs = r1.numeric().mult(s, exponent);
      
      DRes<List<SInt>> bits = new DefaultAdvancedNumeric(r1).toBits(abs, l);

      return () -> new Pair<>(b, bits);
    }).seq((r2, signAndBits) -> {
      
      SInt b = signAndBits.getFirst().out();
      List<SInt> bits = signAndBits.getSecond().out();
      
      RealNumeric rn = new FixedNumeric(r2);
      
      // Result if exponent is positive
      DRes<SReal> bp = rn.known(1.0);

      // Result if exponent is negative
      DRes<SReal> bn = rn.known(1.0);
      
      int f = builder.getBasicNumericContext().getPrecision();
      int l = log2(f);
      
      // TODO: 0'th iteration can be done in init to avoid a truncation
      for (int i = 0; i < l; i++) {
        double twoI = Math.pow(2, i);
        
        DRes<SInt> t = bits.get(i);
        bp = rn.add(bp, new MultiplyWithSInt(rn.sub(rn.mult(Math.pow(2.0, twoI), bp), bp), t).buildComputation(r2));
        bn = rn.add(bn, new MultiplyWithSInt(rn.sub(rn.mult(Math.pow(2.0, -twoI), bn), bn), t).buildComputation(r2));
      }
      
      return new AdvancedFixedNumeric(r2).condSelect(b, bn, bp);
    });
  }

}
