package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Computes the covariance over two data vectors. If the mean is known, a more precise result will
 * be given.
 */
public class Covariance implements Computation<SInt, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> data1;
  private final List<DRes<SInt>> data2;
  private final DRes<SInt> mean1;
  private final DRes<SInt> mean2;


  public Covariance(List<DRes<SInt>> data1, List<DRes<SInt>> data2,
      DRes<SInt> mean1, DRes<SInt> mean2) {
    this.data1 = data1;
    this.data2 = data2;

    if (data1.size() != data2.size()) {
      throw new IllegalArgumentException("Must have same sample size.");
    }

    this.mean1 = mean1;
    this.mean2 = mean2;
  }

  public Covariance(List<DRes<SInt>> data1, List<DRes<SInt>> data2) {
    this(data1, data2, null, null);
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq((seq) -> null
    ).pairInPar(
        (seq, ignored) -> {
          if (mean1 == null) {
            return seq.seq(new Mean(data1));
          } else {
            return mean1;
          }
        },
        (seq, ignored) -> {
          if (mean2 == null) {
            return seq.seq(new Mean(data2));
          } else {
            return mean2;
          }
        }
    ).par((par, means) -> {
      SInt mean1 = means.getFirst();
      SInt mean2 = means.getSecond();
      //Implemented using two iterators instead of indexed loop to avoid enforcing RandomAccess lists
      Iterator<DRes<SInt>> iterator1 = data1.iterator();
      Iterator<DRes<SInt>> iterator2 = data2.iterator();
      List<DRes<SInt>> terms = new ArrayList<>(data1.size());
      while (iterator1.hasNext()) {
        DRes<SInt> value1 = iterator1.next();
        DRes<SInt> value2 = iterator2.next();
        DRes<SInt> term = par.seq((seq) -> {
          Numeric numeric = seq.numeric();
          DRes<SInt> tmp1 = numeric.sub(value1, () -> mean1);
          DRes<SInt> tmp2 = numeric.sub(value2, () -> mean2);
          return numeric.mult(tmp1, tmp2);
        });
        terms.add(term);
      }
      return () -> terms;
    }).seq((seq, terms) -> seq.seq(new Mean(terms, data1.size() - 1))
    );
  }

}
