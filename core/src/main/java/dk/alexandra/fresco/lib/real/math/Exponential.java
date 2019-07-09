package dk.alexandra.fresco.lib.real.math;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.utils.Scaling;

public class Exponential implements Computation<SReal, ProtocolBuilderNumeric> {

  private DRes<SReal> x;

  /**
   * p1045 from "Computer Approximations" by Hart et al. which approximates x -> 2^x
   * on the interval [0, 1].
   */
  private static double[] POLYNOMIAL =
      new double[] {0.1000000077443021686e1, 0.693147180426163827795756e0, 0.24022651071017064605384e0,
          .55504068620466379157744e-1, 0.9618341225880462374977e-2, 0.1332730359281437819329e-2,
          0.155107460590052573978e-3, 0.14197847399765606711e-4, 0.1863347724137967076e-5};
  
  public Exponential(DRes<SReal> x) {
    this.x = x;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    
    // Sign bit
    DRes<SInt> b = builder.realNumeric().leq(x, builder.realNumeric().known(0.0));
        
    return builder.seq(seq -> {
      
      // e^x = 2^{log_2 e * x} = 2^{1.442695040889 * x}
      DRes<SReal> X = seq.realNumeric().mult(1.442695040889, x);
      
      // Sign = 1 - 2b 
      DRes<SInt> s = seq.numeric().add(1, seq.numeric().mult(-2, b));
      
      // Take absolute value
      X = new Scaling(X, s).buildComputation(seq);
      
      // Integer part
      DRes<SInt> xPrime = seq.realAdvanced().floor(X);
      
      // Fractional part
      DRes<SReal> xDoublePrime = seq.realNumeric().sub(X, seq.realNumeric().fromSInt(xPrime));
      
      return () -> new Pair<>(xPrime, xDoublePrime);
    }).seq((par, x) -> {
      
      // 2^integer part
      DRes<SReal> f = par.realAdvanced().twoPower(x.getFirst());      
      
      // 2^fractional part
      DRes<SReal> g =
          par.realAdvanced().polynomialEvalutation(x.getSecond(), POLYNOMIAL);
      
      return () -> new Pair<>(f,g);
    }).seq((seq, fg) -> {
      
      DRes<SReal> h = seq.realNumeric().mult(fg.getFirst(), fg.getSecond());
      DRes<SReal> hRecip = seq.realAdvanced().reciprocal(h);
            
      return seq.realAdvanced().condSelect(b, hRecip, h);
    });
  }

}
