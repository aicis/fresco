package dk.alexandra.fresco.lib.collections.io;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class OpenMatrix
    implements ComputationParallel<Matrix<DRes<BigInteger>>, ProtocolBuilderNumeric> {

  private final DRes<Matrix<DRes<SInt>>> closedMatrix;

  public OpenMatrix(DRes<Matrix<DRes<SInt>>> closedMatrix) {
    super();
    this.closedMatrix = closedMatrix;
  }

  @Override
  public DRes<Matrix<DRes<BigInteger>>> buildComputation(ProtocolBuilderNumeric builder) {
    Collections collections = builder.collections();
    List<DRes<List<DRes<BigInteger>>>> closedRows = new ArrayList<>();
    for (List<DRes<SInt>> row : closedMatrix.out().getRows()) {
      // still sort of hacky: need to artificially wrap row in computation
      DRes<List<DRes<BigInteger>>> closedRow = collections.openList(() -> row);
      closedRows.add(closedRow);
    }
    return () -> new MatrixUtils().unwrapRows(closedRows);
  }
}
