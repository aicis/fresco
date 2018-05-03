package dk.alexandra.fresco.lib.conditional;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.RowPairD;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.util.ArrayList;
import java.util.List;

/**
 * Swaps neighboring rows in a matrix depending on the swapping bits. Swaps are potentially done
 * between rows <code>i</code> and <code>i+1</code> for all even <code>i</code>.
 */
public class SwapNeighborsIf <T extends DRes<SInt>>
implements ComputationParallel<Matrix<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<List<T>> conditions;
  private final DRes<Matrix<T>> rows;

  public SwapNeighborsIf(DRes<List<T>> swappers, DRes<Matrix<T>> rows) {
    this.conditions = swappers;
    this.rows = rows;
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    Collections collections = builder.collections();
    List<T> swappersOut = conditions.out();
    Matrix<T> rowsOut = rows.out();

    List<DRes<RowPairD<SInt, SInt>>> pairs = new ArrayList<>();
    int swapperIdx = 0;
    for (int i = 0; i < rowsOut.getHeight() - 1; i += 2) {
      List<T> tempLeft = rowsOut.getRow(i);
      List<T> tempRight = rowsOut.getRow(i + 1);
      DRes<RowPairD<SInt, SInt>> pair =
          collections.swapIf(swappersOut.get(swapperIdx), () -> tempLeft, () -> tempRight);
      swapperIdx++;
      pairs.add(pair);
    }

    return () -> {
      ArrayList<ArrayList<DRes<SInt>>> temp = new ArrayList<>();
      for (DRes<RowPairD<SInt, SInt>> pair : pairs) {
        temp.add(new ArrayList<>(pair.out().getFirst().out()));
        temp.add(new ArrayList<>(pair.out().getSecond().out()));
      }
      return new Matrix<>(rowsOut.getHeight(), rowsOut.getWidth(), temp);
    };
  }
}
