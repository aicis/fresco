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
public class SwapRowsIf
    implements ComputationParallel<RowPairD<SInt, SInt>, ProtocolBuilderNumeric> {

  private final DRes<SInt> condition;
  private final DRes<List<DRes<SInt>>> left;
  private final DRes<List<DRes<SInt>>> right;

  public SwapRowsIf(DRes<SInt> condition, DRes<List<DRes<SInt>>> left,
      DRes<List<DRes<SInt>>> right) {
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
