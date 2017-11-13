package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes the varians from a list of SInt values and the previously
 * computed {@link Mean mean}.
 */
public class Variance implements Computation<SInt, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> data;
  private final DRes<SInt> mean;

  public Variance(List<DRes<SInt>> data, DRes<SInt> mean) {
    this.data = data;
    this.mean = mean;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par((par) -> {
      List<DRes<SInt>> terms = new ArrayList<>(data.size());
      for (DRes<SInt> value : data) {
        DRes<SInt> term = par.seq((seq) -> {
          Numeric numeric = seq.numeric();
          DRes<SInt> tmp = numeric.sub(value, mean);
          return numeric.mult(tmp, tmp);
        });
        terms.add(term);
      }
      return () -> terms;
    }).seq((seq, terms) -> seq.seq(new Mean(terms, data.size() - 1))
    );
  }

}
