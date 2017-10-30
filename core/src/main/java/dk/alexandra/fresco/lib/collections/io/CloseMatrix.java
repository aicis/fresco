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

public class CloseMatrix
    implements ComputationParallel<Matrix<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final Matrix<BigInteger> openMatrix;
  private final int inputParty;
  private final int height;
  private final int width;
  private final boolean isInputProvider;

  /**
   * See {@link dk.alexandra.fresco.framework.builder.numeric.Collections#closeMatrix(Matrix, int)
   * closeMatrix}.
   */
  public CloseMatrix(Matrix<BigInteger> openMatrix, int inputParty) {
    super();
    this.openMatrix = openMatrix;
    this.height = openMatrix.getHeight();
    this.width = openMatrix.getWidth();
    this.inputParty = inputParty;
    this.isInputProvider = true;
  }

  /**
   * See {@link dk.alexandra.fresco.framework.builder.numeric.Collections#closeMatrix(int, int, int)
   * closeMatrix}.
   */
  public CloseMatrix(int h, int w, int inputParty) {
    super();
    this.openMatrix = null;
    this.height = h;
    this.width = w;
    this.inputParty = inputParty;
    this.isInputProvider = false;
  }

  private List<DRes<List<DRes<SInt>>>> buildAsProvider(Collections collections) {
    List<DRes<List<DRes<SInt>>>> closedRows = new ArrayList<>();
    for (List<BigInteger> row : openMatrix.getRows()) {
      DRes<List<DRes<SInt>>> closedRow = collections.closeList(row, inputParty);
      closedRows.add(closedRow);
    }
    return closedRows;
  }

  private List<DRes<List<DRes<SInt>>>> buildAsReceiver(Collections collections) {
    List<DRes<List<DRes<SInt>>>> closedRows = new ArrayList<>();
    for (int r = 0; r < height; r++) {
      DRes<List<DRes<SInt>>> closedRow = collections.closeList(width, inputParty);
      closedRows.add(closedRow);
    }
    return closedRows;
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    Collections collections = builder.collections();
    List<DRes<List<DRes<SInt>>>> closed =
        isInputProvider ? buildAsProvider(collections) : buildAsReceiver(collections);
    return () -> new MatrixUtils().unwrapRows(closed);
  }
}
