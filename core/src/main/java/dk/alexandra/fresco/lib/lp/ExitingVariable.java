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
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ConditionalSelect;
import dk.alexandra.fresco.lib.compare.eq.FracEq;
import dk.alexandra.fresco.lib.lp.ExitingVariable.ExitingVariableOutput;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
import dk.alexandra.fresco.lib.math.integer.min.MinInfFrac;
import dk.alexandra.fresco.lib.math.integer.min.Minimum;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExitingVariable implements
    ComputationBuilder<ExitingVariableOutput, ProtocolBuilderNumeric> {

  private final LPTableau tableau;
  private final Matrix<Computation<SInt>> updateMatrix;
  private final List<Computation<SInt>> enteringIndex;
  private final List<Computation<SInt>> basis;

  ExitingVariable(
      LPTableau tableau, Matrix<Computation<SInt>> updateMatrix,
      List<Computation<SInt>> enteringIndex,
      List<Computation<SInt>> basis) {
    this.basis = basis;
    if (checkDimensions(tableau, updateMatrix, enteringIndex)) {
      this.tableau = tableau;
      this.updateMatrix = updateMatrix;
      this.enteringIndex = enteringIndex;

    } else {
      throw new MPCException("Dimensions of inputs does not match");
    }
  }

  private boolean checkDimensions(LPTableau tableau, Matrix<Computation<SInt>> updateMatrix,
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
  public Computation<ExitingVariableOutput> build(ProtocolBuilderNumeric builder) {
    int tableauHeight = tableau.getC().getHeight() + 1;
    Computation<SInt> zero = builder.numeric().known(BigInteger.ZERO);
    Computation<SInt> one = builder.numeric().known(BigInteger.ONE);
    return builder.par((par) -> {
      ArrayList<Computation<SInt>> enteringColumn = new ArrayList<>(tableauHeight);
      // Extract entering column
      AdvancedNumericBuilder advanced = par.advancedNumeric();
      for (int i = 0; i < tableauHeight - 1; i++) {
        ArrayList<Computation<SInt>> tableauRow = tableau.getC().getRow(i);
        enteringColumn.add(
            advanced.dot(enteringIndex, tableauRow)
        );
      }
      ArrayList<Computation<SInt>> tableauRow = tableau.getF();
      enteringColumn.add(
          advanced.dot(enteringIndex, tableauRow)
      );
      return () -> enteringColumn;
    }).par((enteringColumn, par) -> {
      // Apply update matrix to entering column
      ArrayList<Computation<SInt>> updatedEnteringColumn = new ArrayList<>(tableauHeight);
      AdvancedNumericBuilder advanced = par.advancedNumeric();
      for (int i = 0; i < tableauHeight; i++) {
        ArrayList<Computation<SInt>> updateRow = updateMatrix.getRow(i);
        updatedEnteringColumn.add(
            advanced.dot(updateRow, enteringColumn)
        );
      }

      // Apply update matrix to the B vector
      ArrayList<Computation<SInt>> updatedB = new ArrayList<>(tableauHeight - 1);
      for (int i = 0; i < tableauHeight - 1; i++) {
        List<Computation<SInt>> updateRow = updateMatrix.getRow(i).subList(0, tableauHeight - 1);
        updatedB.add(
            advanced.dot(updateRow, tableau.getB())
        );
      }
      return Pair.lazy(updatedEnteringColumn, updatedB);
    }).par((pair, par) -> {
      ArrayList<Computation<SInt>> updatedEnteringColumn = pair.getFirst();
      ArrayList<Computation<SInt>> updatedB = pair.getSecond();
      ArrayList<Computation<SInt>> nonApps = new ArrayList<>(updatedB.size());

      ComparisonBuilder comparison = par.comparison();
      for (int i = 0; i < updatedB.size(); i++) {
        nonApps.add(
            comparison.compareLEQLong(updatedEnteringColumn.get(i), zero)
        );
      }
      return Pair.lazy(updatedEnteringColumn, new Pair<>(updatedB, nonApps));
    }).seq((pair, seq) -> {
      ArrayList<Computation<SInt>> updatedEnteringColumn = pair.getFirst();
      ArrayList<Computation<SInt>> updatedB = pair.getSecond().getFirst();
      ArrayList<Computation<SInt>> nonApps = pair.getSecond().getSecond();
      List<Computation<SInt>> shortColumn = updatedEnteringColumn.subList(0, updatedB.size());
      return seq.seq(
          new MinInfFrac(updatedB, shortColumn, nonApps)
      ).par((minInfOutput, par) -> {
        List<Computation<SInt>> ties = new ArrayList<>(updatedB.size());
        // Find index of each entry with the minimal ratio found in previous round
        for (int i = 0; i < updatedB.size(); i++) {
          ties.add(
              par.seq(
                  new FracEq(minInfOutput.nm, minInfOutput.dm, updatedB.get(i), shortColumn.get(i)))
          );
        }
        return () -> ties;
      }).seq((ties, seq2) -> {
        // Construct vector of basis variable indices that are both applicable and
        // which associated row is tie for minimal B to entering column ratio
        // all other entries are set to a value larger than any variable index
        BigInteger upperBound = BigInteger.valueOf(tableau.getC().getWidth() + 2);

        return seq2.par(par2 -> {
          NumericBuilder numeric = par2.numeric();
          List<Computation<SInt>> negTies = ties.stream()
              .map(tie -> numeric.sub(one, tie))
              .collect(Collectors.toList());
          return () -> negTies;
        }).par((negTies, par2) -> {
          NumericBuilder numeric = par2.numeric();
          List<Computation<SInt>> multNonApps = nonApps.stream()
              .map(nonApp -> numeric.mult(upperBound, nonApp))
              .collect(Collectors.toList());
          List<Computation<SInt>> multNegTies = negTies.stream()
              .map(negTie -> numeric.mult(upperBound, negTie))
              .collect(Collectors.toList());
          List<Computation<SInt>> multTies = IntStream.range(0, ties.size())
              .boxed()
              .map(i -> numeric.mult(basis.get(i), ties.get(i)))
              .collect(Collectors.toList());
          return () -> new Pair<>(multNonApps, new Pair<>(multNegTies, multTies));
        }).par((pairs, par2) -> {
          List<Computation<SInt>> multNonApps = pairs.getFirst();
          List<Computation<SInt>> multNegTies = pairs.getSecond().getFirst();
          List<Computation<SInt>> multTies = pairs.getSecond().getSecond();

          List<Computation<SInt>> updatedTies = IntStream.range(0, ties.size())
              .boxed()
              .map(i -> {
                return par2.seq(seq3 -> {
                  NumericBuilder numeric = seq3.numeric();
                  Computation<SInt> mult = numeric.add(multNegTies.get(i), multTies.get(i));
                  return numeric.add(multNonApps.get(i), mult);
                });
                  }
              ).collect(Collectors.toList());
          return () -> updatedTies;
        }).seq((finalTies, seq3) -> {
          // Break ties for exiting index by taking the minimal variable index
          Computation<Pair<List<Computation<SInt>>, SInt>> minOut = new Minimum(finalTies)
              .build(seq3);
          return () -> new Pair<>(minOut.out().getFirst(), updatedEnteringColumn);
        });
      });
    }).par((pair, par) -> {
      List<Computation<SInt>> exitingIndex = pair.getFirst();
      ArrayList<Computation<SInt>> updatedEnteringColumn = pair.getSecond();
      // Compute column for the new update matrix

      ArrayList<Computation<SInt>> updateColumn = new ArrayList<>(tableauHeight);
      for (int i = 0; i < tableauHeight - 1; i++) {
        int finalI = i;
        updateColumn.add(
            par.seq((seq) -> {
              NumericBuilder numeric = seq.numeric();
              Computation<SInt> negativeEntering =
                  numeric.sub(zero, updatedEnteringColumn.get(finalI));
              return seq
                  .seq(new ConditionalSelect(exitingIndex.get(finalI), one, negativeEntering));
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
          ArrayList<Computation<SInt>> updateColumn = pair.getSecond().getSecond();
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
      ArrayList<Computation<SInt>> exitingIndex = new ArrayList<>(pair.getFirst().getFirst());
      ArrayList<Computation<SInt>> updateColumn = new ArrayList<>(pair.getSecond().getFirst());
      return () -> new ExitingVariableOutput(exitingIndex, updateColumn, pivot);
    });
  }

  public static class ExitingVariableOutput {

    final ArrayList<Computation<SInt>> exitingIndex;
    final ArrayList<Computation<SInt>> updateColumn;
    final Computation<SInt> pivot;

    public ExitingVariableOutput(
        ArrayList<Computation<SInt>> exitingIndex,
        ArrayList<Computation<SInt>> updateColumn,
        Computation<SInt> pivot) {
      this.exitingIndex = exitingIndex;
      this.updateColumn = updateColumn;
      this.pivot = pivot;
    }
  }
}
