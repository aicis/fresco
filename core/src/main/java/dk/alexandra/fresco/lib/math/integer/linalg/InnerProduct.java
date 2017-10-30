package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

/**
 * Computes the inner product - i.e. <code>Sum(a[0]*b[1], ..., a[n]*b[n])</code> by first computing
 * all the multiplications in parallel, then summing up.
 */
public class InnerProduct implements Computation<SInt, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> aVector;
  private final List<DRes<SInt>> bVector;

  public InnerProduct(
      List<DRes<SInt>> aVector,
      List<DRes<SInt>> bVector) {
    this.aVector = getRandomAccessList(aVector);
    this.bVector = getRandomAccessList(bVector);
  }

  private List<DRes<SInt>> getRandomAccessList(List<DRes<SInt>> aVector) {
    if (aVector instanceof RandomAccess) {
      return aVector;
    } else {
      return new ArrayList<>(aVector);
    }
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder
        .par(parallel -> {
          List<DRes<SInt>> products = new ArrayList<>(aVector.size());
          Numeric numericBuilder = parallel.numeric();
          for (int i = 0; i < aVector.size(); i++) {
            DRes<SInt> nextA = aVector.get(i);
            DRes<SInt> nextB = bVector.get(i);
            products.add(numericBuilder.mult(nextA, nextB));
          }
          return () -> products;
        })
        .seq((seq, list) -> seq.advancedNumeric().sum(list)
        );
  }
}
