package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.compare.eq.FracEq;
import dk.alexandra.fresco.lib.conditional.ConditionalSelect;
import dk.alexandra.fresco.lib.lp.ExitingVariable.ExitingVariableOutput;
import dk.alexandra.fresco.lib.math.integer.min.MinInfFrac;
import dk.alexandra.fresco.lib.math.integer.min.Minimum;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExitingVariable implements
    Computation<ExitingVariableOutput, ProtocolBuilderNumeric> {

  private final LPTableau tableau;
  private final Matrix<DRes<SInt>> updateMatrix;
  private final List<DRes<SInt>> enteringIndex;
  private final List<DRes<SInt>> basis;

  protected ExitingVariable(LPTableau tableau, Matrix<DRes<SInt>> updateMatrix,
      List<DRes<SInt>> enteringIndex, List<DRes<SInt>> basis) {
    this.basis = basis;
    this.tableau = tableau;
    this.updateMatrix = updateMatrix;
    this.enteringIndex = enteringIndex;
  }

  @Override
  public DRes<ExitingVariableOutput> buildComputation(ProtocolBuilderNumeric builder) {
    int tableauHeight = tableau.getC().getHeight() + 1;
    DRes<SInt> zero = builder.numeric().known(BigInteger.ZERO);
    DRes<SInt> one = builder.numeric().known(BigInteger.ONE);
    return builder.par((par) -> {
      ArrayList<DRes<SInt>> enteringColumn = new ArrayList<>(tableauHeight);
      // Extract entering column
      AdvancedNumeric advanced = par.advancedNumeric();
      for (int i = 0; i < tableauHeight - 1; i++) {
        ArrayList<DRes<SInt>> tableauRow = tableau.getC().getRow(i);
        enteringColumn.add(
            advanced.innerProduct(enteringIndex, tableauRow)
        );
      }
      ArrayList<DRes<SInt>> tableauRow = tableau.getF();
      enteringColumn.add(
          advanced.innerProduct(enteringIndex, tableauRow)
      );
      return () -> enteringColumn;
    }).par((par, enteringColumn) -> {
      // Apply update matrix to entering column
      ArrayList<DRes<SInt>> updatedEnteringColumn = new ArrayList<>(tableauHeight);
      AdvancedNumeric advanced = par.advancedNumeric();
      for (int i = 0; i < tableauHeight; i++) {
        ArrayList<DRes<SInt>> updateRow = updateMatrix.getRow(i);
        updatedEnteringColumn.add(
            advanced.innerProduct(updateRow, enteringColumn)
        );
      }

      // Apply update matrix to the B vector
      ArrayList<DRes<SInt>> updatedB = new ArrayList<>(tableauHeight - 1);
      for (int i = 0; i < tableauHeight - 1; i++) {
        List<DRes<SInt>> updateRow = updateMatrix.getRow(i).subList(0, tableauHeight - 1);
        updatedB.add(
            advanced.innerProduct(updateRow, tableau.getB())
        );
      }
      return Pair.lazy(updatedEnteringColumn, updatedB);
    }).par((par, pair) -> {
      ArrayList<DRes<SInt>> updatedEnteringColumn = pair.getFirst();
      ArrayList<DRes<SInt>> updatedB = pair.getSecond();
      ArrayList<DRes<SInt>> nonApps = new ArrayList<>(updatedB.size());

      Comparison comparison = par.comparison();
      for (int i = 0; i < updatedB.size(); i++) {
        nonApps.add(
            comparison.compareLEQLong(updatedEnteringColumn.get(i), zero)
        );
      }
      return Pair.lazy(updatedEnteringColumn, new Pair<>(updatedB, nonApps));
    }).seq((seq, pair) -> {
      ArrayList<DRes<SInt>> updatedEnteringColumn = pair.getFirst();
      ArrayList<DRes<SInt>> updatedB = pair.getSecond().getFirst();
      ArrayList<DRes<SInt>> nonApps = pair.getSecond().getSecond();
      List<DRes<SInt>> shortColumn = updatedEnteringColumn.subList(0, updatedB.size());
      return seq.seq(
          new MinInfFrac(updatedB, shortColumn, nonApps)
      ).par((par, minInfOutput) -> {
        List<DRes<SInt>> ties = new ArrayList<>(updatedB.size());
        // Find index of each entry with the minimal ratio found in previous round
        for (int i = 0; i < updatedB.size(); i++) {
          ties.add(
              par.seq(
                  new FracEq(minInfOutput.nm, minInfOutput.dm, updatedB.get(i), shortColumn.get(i)))
          );
        }
        return () -> ties;
      }).seq((seq2, ties) -> {
        // Construct vector of basis variable indices that are both applicable and
        // which associated row is tie for minimal B to entering column ratio
        // all other entries are set to a value larger than any variable index
        BigInteger upperBound = BigInteger.valueOf(tableau.getC().getWidth() + 2);

        return seq2.par(par2 -> {
          Numeric numeric = par2.numeric();
          List<DRes<SInt>> negTies = ties.stream()
              .map(tie -> numeric.sub(one, tie))
              .collect(Collectors.toList());
          return () -> negTies;
        }).par((par2, negTies) -> {
          Numeric numeric = par2.numeric();
          List<DRes<SInt>> multNonApps = nonApps.stream()
              .map(nonApp -> numeric.mult(upperBound, nonApp))
              .collect(Collectors.toList());
          List<DRes<SInt>> multNegTies = negTies.stream()
              .map(negTie -> numeric.mult(upperBound, negTie))
              .collect(Collectors.toList());
          List<DRes<SInt>> multTies = IntStream.range(0, ties.size())
              .boxed()
              .map(i -> numeric.mult(basis.get(i), ties.get(i)))
              .collect(Collectors.toList());
          return () -> new Pair<>(multNonApps, new Pair<>(multNegTies, multTies));
        }).par((par2, pairs) -> {
          List<DRes<SInt>> multNonApps = pairs.getFirst();
          List<DRes<SInt>> multNegTies = pairs.getSecond().getFirst();
          List<DRes<SInt>> multTies = pairs.getSecond().getSecond();

          List<DRes<SInt>> updatedTies = IntStream.range(0, ties.size())
              .boxed()
              .map(i -> {
                return par2.seq(seq3 -> {
                  Numeric numeric = seq3.numeric();
                  DRes<SInt> mult = numeric.add(multNegTies.get(i), multTies.get(i));
                  return numeric.add(multNonApps.get(i), mult);
                });
                  }
              ).collect(Collectors.toList());
          return () -> updatedTies;
        }).seq((seq3, finalTies) -> {
          // Break ties for exiting index by taking the minimal variable index
          DRes<Pair<List<DRes<SInt>>, SInt>> minOut = new Minimum(finalTies)
              .buildComputation(seq3);
          return () -> new Pair<>(minOut.out().getFirst(), updatedEnteringColumn);
        });
      });
    }).par((par, pair) -> {
      List<DRes<SInt>> exitingIndex = pair.getFirst();
      ArrayList<DRes<SInt>> updatedEnteringColumn = pair.getSecond();
      // Compute column for the new update matrix

      ArrayList<DRes<SInt>> updateColumn = new ArrayList<>(tableauHeight);
      for (int i = 0; i < tableauHeight - 1; i++) {
        int finalI = i;
        updateColumn.add(
            par.seq((seq) -> {
              Numeric numeric = seq.numeric();
              DRes<SInt> negativeEntering =
                  numeric.sub(zero, updatedEnteringColumn.get(finalI));
              return seq
                  .seq(new ConditionalSelect(exitingIndex.get(finalI), one, negativeEntering));
            }));
      }
      updateColumn.add(par.numeric().sub(zero, updatedEnteringColumn.get(tableauHeight - 1)));
      return Pair.lazy(exitingIndex, new Pair<>(updatedEnteringColumn, updateColumn));
    }).pairInPar(
        (seq, pair) -> {
          List<DRes<SInt>> updatedEnteringColumn = pair.getSecond().getFirst();
          DRes<SInt> sum = seq.advancedNumeric().sum(
              updatedEnteringColumn.subList(0, tableauHeight - 1));
          //Propagate exiting variable forward
          return Pair.lazy(pair.getFirst(), sum);
        },
        (seq, pair) -> {
          ArrayList<DRes<SInt>> updateColumn = pair.getSecond().getSecond();
          DRes<SInt> sum = seq.advancedNumeric()
              .sum(updateColumn.subList(0, tableauHeight - 1));
          return Pair.lazy(updateColumn, sum);
        }
    ).seq((seq, pair) -> {
      // Determine pivot
      DRes<SInt> sumEnteringColumn = pair.getFirst().getSecond();
      DRes<SInt> sumUpdateColumn = pair.getSecond().getSecond();
      Numeric numeric = seq.numeric();
      DRes<SInt> finalSum = numeric.add(sumEnteringColumn, sumUpdateColumn);
      DRes<SInt> pivot = numeric.sub(finalSum, one);
      ArrayList<DRes<SInt>> exitingIndex = new ArrayList<>(pair.getFirst().getFirst());
      ArrayList<DRes<SInt>> updateColumn = new ArrayList<>(pair.getSecond().getFirst());
      return () -> new ExitingVariableOutput(exitingIndex, updateColumn, pivot);
    });
  }

  public static class ExitingVariableOutput {

    final ArrayList<DRes<SInt>> exitingIndex;
    final ArrayList<DRes<SInt>> updateColumn;
    final DRes<SInt> pivot;

    public ExitingVariableOutput(
        ArrayList<DRes<SInt>> exitingIndex,
        ArrayList<DRes<SInt>> updateColumn,
        DRes<SInt> pivot) {
      this.exitingIndex = exitingIndex;
      this.updateColumn = updateColumn;
      this.pivot = pivot;
    }
  }
}
