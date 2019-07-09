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

  public Exponential(DRes<SReal> x) {
    this.x = x;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    
    // Sign bit
    DRes<SInt> b = builder.realNumeric().leq(x, builder.realNumeric().known(0.0));
    
    // Sign
    DRes<SInt> s = builder.numeric().add(1, builder.numeric().mult(-2, b));
    
    return builder.seq(seq -> {
      DRes<SReal> X = seq.realNumeric().mult(1.442695040889, x);      
      X = new Scaling(X, s).buildComputation(seq);
      
      DRes<SInt> xPrime = seq.realAdvanced().floor(X);
      DRes<SReal> xDoublePrime = seq.realNumeric().sub(X, seq.realNumeric().fromSInt(xPrime));
      return () -> new Pair<>(xPrime, xDoublePrime);
    }).seq((par, x) -> {
      
      DRes<SReal> f = par.realAdvanced().twoPower(x.getFirst());      
      DRes<SReal> g =
          par.realAdvanced().polynomialEvalutation(x.getSecond(), ApproximationPolynomials.TWOPOW);
      return () -> new Pair<>(f,g);
    }).seq((seq, fg) -> {
      
      DRes<SReal> h = seq.realNumeric().mult(fg.getFirst(), fg.getSecond());
      DRes<SReal> hRecip = seq.realAdvanced().reciprocal(h);
            
      return seq.realAdvanced().condSelect(b, hRecip, h);
    });
  }

}
