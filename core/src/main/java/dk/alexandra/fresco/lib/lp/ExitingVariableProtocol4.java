/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ConditionalSelect;
import dk.alexandra.fresco.lib.lp.ExitingVariableProtocol4.ExitingVariableOutput;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
import dk.alexandra.fresco.lib.math.integer.min.MinInfFracProtocol4;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExitingVariableProtocol4 implements ComputationBuilder<ExitingVariableOutput> {

  private final LPTableau tableau;
  private final Matrix4<Computation<SInt>> updateMatrix;
  private final List<Computation<SInt>> enteringIndex;

  ExitingVariableProtocol4(
      LPTableau tableau, Matrix4<Computation<SInt>> updateMatrix,
      List<Computation<SInt>> enteringIndex) {
    if (checkDimensions(tableau, updateMatrix, enteringIndex)) {
      this.tableau = tableau;
      this.updateMatrix = updateMatrix;
      this.enteringIndex = enteringIndex;

    } else {
      throw new MPCException("Dimensions of inputs does not match");
    }
  }

  private boolean checkDimensions(LPTableau tableau, Matrix4<Computation<SInt>> updateMatrix,
      List<Computation<SInt>> enteringIndex) {
    int updateHeight = updateMatrix.getHeight();
    int updateWidth = updateMatrix.getWidth();
    int tableauHeight = tableau.getC().getHeight() + 1;
    int tableauWidth = tableau.getC().getWidth() + 1;
    return (updateHeight == updateWidth &&
        updateHeight == tableauHeight &&
        enteringIndex.size() == tableauWidth - 1);
  }


  @Override
  public Computation<ExitingVariableOutput> build(SequentialProtocolBuilder builder) {
    int tableauHeight = tableau.getC().getHeight() + 1;
    Computation<SInt> zero = builder.numeric().known(BigInteger.ZERO);
    Computation<SInt> one = builder.numeric().known(BigInteger.ONE);
    return builder.par((par) -> {
      List<Computation<SInt>> enteringColumn = new ArrayList<>(tableauHeight);
      // Extract entering column
      AdvancedNumericBuilder advanced = par.createAdvancedNumericBuilder();
      for (int i = 0; i < tableauHeight - 1; i++) {
        SInt[] tableauRow = tableau.getC().getIthRow(i);
        enteringColumn.add(
            advanced.dot(enteringIndex, Arrays.asList(tableauRow))
        );
      }
      SInt[] tableauRow = tableau.getF();
      enteringColumn.add(
          advanced.dot(enteringIndex, Arrays.asList(tableauRow))
      );
      return () -> enteringColumn;
    }).par((enteringColumn, par) -> {
      // Apply update matrix to entering column
      List<Computation<SInt>> updatedEnteringColumn = new ArrayList<>(tableauHeight);
      AdvancedNumericBuilder advanced = par.createAdvancedNumericBuilder();
      for (int i = 0; i < tableauHeight; i++) {
        ArrayList<Computation<SInt>> updateRow = updateMatrix.getRow(i);
        updatedEnteringColumn.add(
            advanced.dot(updateRow, enteringColumn)
        );
      }

      // Apply update matrix to the B vector
      List<Computation<SInt>> updatedB = new ArrayList<>(tableauHeight - 1);
      for (int i = 0; i < tableauHeight - 1; i++) {
        List<Computation<SInt>> updateRow = updateMatrix.getRow(i).subList(0, tableauHeight - 1);
        updatedB.add(
            advanced.dot(updateRow, Arrays.asList(tableau.getB()))
        );
      }
      return Pair.lazy(updatedEnteringColumn, updatedB);
    }).par((pair, par) -> {
      List<Computation<SInt>> updatedEnteringColumn = pair.getFirst();
      List<Computation<SInt>> updatedB = pair.getSecond();
      List<Computation<SInt>> nonApps = new ArrayList<>(updatedB.size());

      ComparisonBuilder comparison = par.comparison();
      for (int i = 0; i < updatedB.size(); i++) {
        nonApps.add(
            comparison.compareLong(updatedEnteringColumn.get(i), zero)
        );
      }
      return Pair.lazy(updatedEnteringColumn, new Pair<>(updatedB, nonApps));
    }).seq((pair, seq) -> {
      List<Computation<SInt>> updatedEnteringColumn = pair.getFirst();
      List<Computation<SInt>> updatedB = pair.getSecond().getFirst();
      List<Computation<SInt>> nonApps = pair.getSecond().getSecond();
      List<Computation<SInt>> shortColumn = updatedEnteringColumn.subList(0, updatedB.size());
      Computation<List<Computation<SInt>>> exitingIndexComputation = seq.createSequentialSub(
          new MinInfFracProtocol4(updatedB, shortColumn, nonApps));
      return () -> new Pair<>(exitingIndexComputation.out(), updatedEnteringColumn);
    }).par((pair, par) -> {
      List<Computation<SInt>> exitingIndex = pair.getFirst();
      List<Computation<SInt>> updatedEnteringColumn = pair.getSecond();
      // Compute column for the new update matrix

      List<Computation<SInt>> updateColumn = new ArrayList<>(tableauHeight);
      for (int i = 0; i < tableauHeight - 1; i++) {
        int finalI = i;
        updateColumn.add(
            par.createSequentialSub((seq) -> {
              NumericBuilder numeric = seq.numeric();
              Computation<SInt> negativeEntering =
                  numeric.sub(zero, updatedEnteringColumn.get(finalI));
              return seq.createSequentialSub(
                  new ConditionalSelect(exitingIndex.get(finalI), one, negativeEntering));
            }));
      }
      updateColumn.add(par.numeric().sub(zero, updatedEnteringColumn.get(tableauHeight - 1)));
      return Pair.lazy(exitingIndex, new Pair<>(updatedEnteringColumn, updateColumn));
    }).par(
        (pair, seq) -> {
          List<Computation<SInt>> updatedEnteringColumn = pair.getSecond().getFirst();
          Computation<SInt> sum = new SumSIntList(
              updatedEnteringColumn.subList(0, tableauHeight - 1)).build(seq);
          //Propagate exiting variable forward
          return Pair.lazy(pair.getFirst(), sum);
        },
        (pair, seq) -> {
          List<Computation<SInt>> updateColumn = pair.getSecond().getSecond();
          Computation<SInt> sum = new SumSIntList(updateColumn.subList(0, tableauHeight - 1))
              .build(seq);
          return Pair.lazy(updateColumn, sum);
        }
    ).seq((pair, seq) -> {
      // Determine pivot
      Computation<SInt> sumEnteringColumn = pair.getFirst().getSecond();
      Computation<SInt> sumUpdateColumn = pair.getSecond().getSecond();
      NumericBuilder numeric = seq.numeric();
      Computation<SInt> finalSum = numeric.add(sumEnteringColumn, sumUpdateColumn);
      Computation<SInt> pivot = numeric.sub(finalSum, one);
      List<Computation<SInt>> exitingIndex = pair.getFirst().getFirst();
      List<Computation<SInt>> updateColumn = pair.getSecond().getFirst();
      return () -> new ExitingVariableOutput(exitingIndex, updateColumn, pivot);
    });
  }

  public static class ExitingVariableOutput {

    final List<Computation<SInt>> exitingIndex;
    final List<Computation<SInt>> updateColumn;
    final Computation<SInt> pivot;

    public ExitingVariableOutput(
        List<Computation<SInt>> exitingIndex,
        List<Computation<SInt>> updateColumn,
        Computation<SInt> pivot) {
      this.exitingIndex = exitingIndex;
      this.updateColumn = updateColumn;
      this.pivot = pivot;
    }
  }
}
