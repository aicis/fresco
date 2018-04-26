package dk.alexandra.fresco.lib.conditional;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.RowPairD;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

/**
 * Swaps the two rows if the condition bit is true, and does nothing if false.
 */
public class SwapRowsIf<T extends DRes<SInt>>
    implements ComputationParallel<RowPairD<SInt, SInt>, ProtocolBuilderNumeric> {

  private final DRes<SInt> condition;
  private final DRes<List<T>> left;
  private final DRes<List<T>> right;

  public SwapRowsIf(DRes<SInt> condition, DRes<List<T>> left, DRes<List<T>> right) {
    this.condition = condition;
    this.left = left;
    this.right = right;
  }

  @Override
  public DRes<RowPairD<SInt, SInt>> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<List<DRes<SInt>>> updatedLeft = builder.collections().condSelect(condition, right, left);
    DRes<List<DRes<SInt>>> updatedRight = builder.collections().condSelect(condition, left, right);
    return () -> new RowPairD<>(updatedLeft, updatedRight);
  }
}
