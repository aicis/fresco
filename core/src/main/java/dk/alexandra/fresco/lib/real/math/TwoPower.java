package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.utils.Scaling;

/**
 * Compute 2 to a secret power <i>n</i> which may be negative and <i>abs(n)</i> should be smaller
 * than the fixed point precision.
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
    int f = builder.getRealNumericContext().getPrecision();
    int l = log2(f);
    
    // Sign bit
    DRes<SInt> b = builder.comparison().compareLEQ(exponent, builder.numeric().known(0));
    
    // Sign
    DRes<SInt> s = builder.numeric().add(1, builder.numeric().mult(-2, b));
    
    // Absolute value of n
    DRes<SInt> n = builder.numeric().mult(s, exponent);
    
    return builder.seq(seq -> {
      return seq.advancedNumeric().toBits(n, l);
    }).seq((seq, bits) -> {
      RealNumeric rn = seq.realNumeric();
      
      // Result if exponent is positive
      DRes<SReal> bp = seq.realNumeric().known(1.0);

      // Result if exponent is negative
      DRes<SReal> bn = seq.realNumeric().known(1.0);
      
      // TODO: 0'th iteration can be done in init to avoid shifts
      for (int i = 0; i < l; i++) {
        double twoI = Math.pow(2, i);
        
        DRes<SInt> t = bits.get(i);
        bp = rn.add(bp, new Scaling(rn.sub(rn.mult(Math.pow(2.0, twoI), bp), bp), t).buildComputation(seq));
        bn = rn.add(bn, new Scaling(rn.sub(rn.mult(Math.pow(2.0, -twoI), bn), bn), t).buildComputation(seq));
      }
      
      return seq.realAdvanced().condSelect(b, bn, bp);      
    });
  }

}
