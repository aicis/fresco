package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.math.Exponential;
import dk.alexandra.fresco.lib.real.math.Logarithm;
import dk.alexandra.fresco.lib.real.math.PolynomialEvaluation;
import dk.alexandra.fresco.lib.real.math.Reciprocal;
import dk.alexandra.fresco.lib.real.math.SquareRoot;
import dk.alexandra.fresco.lib.real.math.TwoPower;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultAdvancedRealNumeric implements AdvancedRealNumeric {

  protected final ProtocolBuilderNumeric builder;

  protected DefaultAdvancedRealNumeric(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SReal> sum(List<DRes<SReal>> input) {
    return builder.seq(seq -> () -> input)
        .whileLoop((inputs) -> inputs.size() > 1, (seq, inputs) -> seq.par(parallel -> {
          List<DRes<SReal>> out = new ArrayList<>();
          DRes<SReal> left = null;
          for (DRes<SReal> input1 : inputs) {
            if (left == null) {
              left = input1;
            } else {
              out.add(parallel.realNumeric().add(left, input1));
              left = null;
            }
          }
          if (left != null) {
            out.add(left);
          }
          return () -> out;
        })).seq((r3, currentInput) -> {
          return currentInput.get(0);
        });
  }

  @Override
  public DRes<SReal> exp(DRes<SReal> x) {    
    return new Exponential(x).buildComputation(builder);
  }

  @Override
  public DRes<SReal> log(DRes<SReal> x) {
    return new Logarithm(x).buildComputation(builder);
  }


  @Override
  public DRes<SReal> reciprocal(DRes<SReal> x) {
    return new Reciprocal(x).buildComputation(builder);
  }

  @Override
  public DRes<SReal> sqrt(DRes<SReal> x) {
    return new SquareRoot(x).buildComputation(builder);
  }
  
  @Override
  public DRes<SReal> twoPower(DRes<SInt> x) {
    return new TwoPower(x).buildComputation(builder);
  }

  @Override
  public DRes<SReal> polynomialEvalutation(DRes<SReal> input, double ... polynomial) {
    return new PolynomialEvaluation(input, polynomial).buildComputation(builder);
  }

}
