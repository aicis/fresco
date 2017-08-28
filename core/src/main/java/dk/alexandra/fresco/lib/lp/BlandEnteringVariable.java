/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 */
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BlandEnteringVariable
    implements ComputationBuilder<Pair<List<Computation<SInt>>, SInt>, ProtocolBuilderNumeric> {

  private final LPTableau tableau;
  private final Matrix<Computation<SInt>> updateMatrix;

  public BlandEnteringVariable(LPTableau tableau,
      Matrix<Computation<SInt>> updateMatrix) {
    if (checkDimensions(tableau, updateMatrix)) {
      this.updateMatrix = updateMatrix;
      this.tableau = tableau;
    } else {
      throw new MPCException("Dimensions of inputs do not match");
    }
  }

  private boolean checkDimensions(LPTableau tableau, Matrix<Computation<SInt>> updateMatrix) {
    int updateHeight = updateMatrix.getHeight();
    int updateWidth = updateMatrix.getWidth();
    int tableauHeight = tableau.getC().getHeight() + 1;
    int tableauWidth = tableau.getC().getWidth() + 1;
    return (updateHeight == updateWidth && updateHeight == tableauHeight);
        /*&& enteringIndex.length == tableauWidth - 1*/
  }

  @Override
  public Computation<Pair<List<Computation<SInt>>, SInt>> buildComputation(
      ProtocolBuilderNumeric builder) {
    Computation<SInt> negativeOne = builder.numeric().known(BigInteger.valueOf(-1));
    Computation<SInt> one = builder.numeric().known(BigInteger.ONE);
    return builder.par(par -> {
      int updateVectorDimension = updateMatrix.getHeight();
      int numOfFs = tableau.getF().size();
      List<Computation<SInt>> updatedF = new ArrayList<>(numOfFs);
      ArrayList<Computation<SInt>> updateVector = updateMatrix.getRow(updateVectorDimension - 1);
      for (int i = 0; i < numOfFs; i++) {
        List<Computation<SInt>> constraintColumn = new ArrayList<>(updateVectorDimension);
        constraintColumn.addAll(tableau.getC().getColumn(i));
        constraintColumn.add(tableau.getF().get(i));

        AdvancedNumericBuilder advancedNumericBuilder = par.advancedNumeric();
        updatedF.add(
            advancedNumericBuilder.dot(
                constraintColumn,
                updateVector)
        );
      }
      return () -> updatedF;
    }).seq((seq, updatedF) ->
        seq.par(par -> {
          ArrayList<Computation<SInt>> signs = new ArrayList<>(updatedF.size());
          for (Computation<SInt> f : updatedF) {
            signs.add(par.comparison().compareLEQ(f, negativeOne));
          }
          return () -> signs;
        }).seq((seq2, signs) -> {
          //Prefix sum
          ArrayList<Computation<SInt>> updatedSigns = new ArrayList<>();
          updatedSigns.add(signs.get(0));
          Computation<SInt> previous = signs.get(0);
          for (int i = 1; i < signs.size(); i++) {
            Computation<SInt> current = signs.get(i);
            previous = seq2.numeric().add(previous, current);
            updatedSigns.add(previous);
          }
          return () -> updatedSigns;
        }).par((par, signs) -> {
          //Pairwise sums
          ArrayList<Computation<SInt>> pairwiseSums = new ArrayList<>();
          pairwiseSums.add(signs.get(0));
          for (int i = 1; i < signs.size(); i++) {
            pairwiseSums.add(par.numeric().add(signs.get(i - 1), signs.get(i)));
          }
          return () -> pairwiseSums;
        }).par((par, pairwiseSums) -> {
          ArrayList<Computation<SInt>> enteringIndex = new ArrayList<>();
          int bitlength = (int) Math.log(pairwiseSums.size()) * 2 + 1;
          ComparisonBuilder comparison = par.comparison();
          for (int i = 0; i < updatedF.size(); i++) {
            enteringIndex.add(comparison.equals(bitlength, pairwiseSums.get(i), one));
          }
          return () -> enteringIndex;
        })).seq((seq, enteringIndex) -> {
      Computation<SInt> terminationSum = seq.seq(new SumSIntList(enteringIndex));
      Computation<SInt> termination = seq.numeric().sub(one, terminationSum);
      return () -> new Pair<>(enteringIndex, termination.out());
    });
  }
}
