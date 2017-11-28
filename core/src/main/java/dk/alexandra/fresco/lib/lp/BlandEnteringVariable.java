package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BlandEnteringVariable
    implements Computation<Pair<List<DRes<SInt>>, SInt>, ProtocolBuilderNumeric> {

  private final LPTableau tableau;
  private final Matrix<DRes<SInt>> updateMatrix;

  protected BlandEnteringVariable(LPTableau tableau, Matrix<DRes<SInt>> updateMatrix) {
    this.updateMatrix = updateMatrix;
    this.tableau = tableau;
  }

  @Override
  public DRes<Pair<List<DRes<SInt>>, SInt>> buildComputation(
      ProtocolBuilderNumeric builder) {
    DRes<SInt> negativeOne = builder.numeric().known(BigInteger.valueOf(-1));
    DRes<SInt> one = builder.numeric().known(BigInteger.ONE);
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
        seq.par(par -> {
          ArrayList<DRes<SInt>> signs = new ArrayList<>(updatedF.size());
          for (DRes<SInt> f : updatedF) {
            signs.add(par.comparison().compareLEQ(f, negativeOne));
          }
          return () -> signs;
        }).seq((seq2, signs) -> {
          //Prefix sum
          ArrayList<DRes<SInt>> updatedSigns = new ArrayList<>();
          updatedSigns.add(signs.get(0));
          DRes<SInt> previous = signs.get(0);
          for (int i = 1; i < signs.size(); i++) {
            DRes<SInt> current = signs.get(i);
            previous = seq2.numeric().add(previous, current);
            updatedSigns.add(previous);
          }
          return () -> updatedSigns;
        }).par((par, signs) -> {
          //Pairwise sums
          ArrayList<DRes<SInt>> pairwiseSums = new ArrayList<>();
          pairwiseSums.add(signs.get(0));
          for (int i = 1; i < signs.size(); i++) {
            pairwiseSums.add(par.numeric().add(signs.get(i - 1), signs.get(i)));
          }
          return () -> pairwiseSums;
        }).par((par, pairwiseSums) -> {
          ArrayList<DRes<SInt>> enteringIndex = new ArrayList<>();
          int bitlength = (int) Math.log(pairwiseSums.size()) * 2 + 1;
          Comparison comparison = par.comparison();
          for (int i = 0; i < updatedF.size(); i++) {
            enteringIndex.add(comparison.equals(bitlength, pairwiseSums.get(i), one));
          }
          return () -> enteringIndex;
        })).seq((seq, enteringIndex) -> {
          DRes<SInt> terminationSum = seq.advancedNumeric().sum(enteringIndex);
          DRes<SInt> termination = seq.numeric().sub(one, terminationSum);
          return () -> new Pair<>(enteringIndex, termination.out());
        });
  }
}
