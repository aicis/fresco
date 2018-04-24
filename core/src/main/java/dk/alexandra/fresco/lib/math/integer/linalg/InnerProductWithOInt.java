package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes the inner product - i.e. <code>Sum(a[0]*b[1], ..., a[n]*b[n])</code> by first computing
 * all the multiplications in parallel, then summing up.
 */
public class InnerProductWithOInt implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<List<DRes<OInt>>> vectorADef;
  private final DRes<List<DRes<SInt>>> vectorBDef;

  public InnerProductWithOInt(
      DRes<List<DRes<OInt>>> vectorA,
      DRes<List<DRes<SInt>>> vectorB) {
    this.vectorADef = vectorA;
    this.vectorBDef = vectorB;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<OInt>> vectorA = vectorADef.out();
    List<DRes<SInt>> vectorB = vectorBDef.out();
    DRes<List<DRes<SInt>>> product = builder.par(parallel -> {
      List<DRes<SInt>> result = new ArrayList<>(vectorA.size());
      Numeric numericBuilder = parallel.numeric();
      for (int i = 0; i < vectorA.size(); i++) {
        DRes<OInt> nextA = vectorA.get(i);
        DRes<SInt> nextB = vectorB.get(i);
        result.add(numericBuilder.multByOpen(nextA, nextB));
      }
      return () -> result;
    });
    return builder.advancedNumeric().sum(product);
  }
}
