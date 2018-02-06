package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Computes the inner product - i.e. <code>Sum(a[0]*b[1], ..., a[n]*b[n])</code> by first computing
 * all the multiplications in parallel, then summing up.
 */
public class InnerProductOpen implements Computation<SInt, ProtocolBuilderNumeric> {

  private final List<BigInteger> vectorA;
  private final List<DRes<SInt>> vectorB;

  public InnerProductOpen(List<BigInteger> vectorA,
      List<DRes<SInt>> vectorB) {
    this.vectorA = vectorA;
    this.vectorB = vectorB;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(parallel -> {
      List<DRes<SInt>> result = new ArrayList<>(vectorA.size());
      Numeric numericBuilder = parallel.numeric();
      for (int i = 0; i < vectorA.size(); i++) {
        BigInteger nextA = vectorA.get(i);
        DRes<SInt> nextB = vectorB.get(i);
        result.add(numericBuilder.mult(nextA, nextB));
      }
      return () -> result;
    }).seq((seq, list) -> seq.advancedNumeric().sum(list));
  }
}