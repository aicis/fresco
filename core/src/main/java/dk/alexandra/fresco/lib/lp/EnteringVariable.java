package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.math.integer.min.Minimum;
import java.util.ArrayList;
import java.util.List;

public class EnteringVariable
    implements Computation<Pair<List<DRes<SInt>>, SInt>, ProtocolBuilderNumeric> {

  private final LPTableau tableau;
  private final Matrix<DRes<SInt>> updateMatrix;

  /**
   * Creates an entering variable computation.
   * 
   * @param tableau an (m + 1)x(n + m + 1) tableau
   * @param updateMatrix an (m + 1)x(m + 1) update matrix, multiplying the tableau on the left with
   */
  protected EnteringVariable(LPTableau tableau, Matrix<DRes<SInt>> updateMatrix) {
    this.updateMatrix = updateMatrix;
    this.tableau = tableau;
  }

  @Override
  public DRes<Pair<List<DRes<SInt>>, SInt>> buildComputation(
      ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      int updateVectorDimension = updateMatrix.getHeight();
      int numOfFs = tableau.getF().size();
      List<DRes<SInt>> updatedF = new ArrayList<>(numOfFs);
      ArrayList<DRes<SInt>> updateVector = updateMatrix.getRow(updateVectorDimension - 1);
      for (int i = 0; i < numOfFs; i++) {
        List<DRes<SInt>> constraintColumn = new ArrayList<>(updateVectorDimension);
        constraintColumn.addAll(tableau.getC().getColumn(i));
        constraintColumn.add(tableau.getF().get(i));

        AdvancedNumeric advancedNumericBuilder = par.advancedNumeric();
        updatedF.add(
            advancedNumericBuilder.innerProduct(
                constraintColumn,
                updateVector)
        );
      }
      return () -> updatedF;
    }).seq((seq, updatedF) ->
        new Minimum(updatedF).buildComputation(seq)
    );
  }
}
