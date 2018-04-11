package dk.alexandra.fresco.lib.collections.batch;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

public class BatchMultiply implements
    ComputationParallel<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<List<DRes<SInt>>> left;
  private final DRes<List<DRes<SInt>>> right;

  public BatchMultiply(
      DRes<List<DRes<SInt>>> left,
      DRes<List<DRes<SInt>>> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    Numeric nb = builder.numeric();
    List<DRes<SInt>> leftOut = left.out();
    List<DRes<SInt>> rightOut = right.out();
    List<DRes<SInt>> products = new ArrayList<>(leftOut.size());
    for (int i = 0; i < leftOut.size(); i++) {
      products.add(nb.mult(leftOut.get(i), rightOut.get(i)));
    }
    return () -> products;
  }

}
