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
 *******************************************************************************/
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ConditionalSelect;
import dk.alexandra.fresco.lib.debug.MarkerProtocolImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.lp.LPSolverProtocol4.LPOutput;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A protocol for solving LP problems using the Simplex method.
 *
 * <p>
 * We basically use the method of
 * <a href="http://fc09.ifca.ai/papers/69_Solving_linear_programs.pdf">Toft 2009</a>.
 *
 * We optimize this protocol by using the so called <i>Revised Simplex</i> method.
 * I.e., instead of updating the tableau it self, we keep track of much smaller
 * update matrix representing changes to the initial tableau. Do this only requires
 * multiplication with a small sparse matrix, which can be done more efficiently
 * than general matrix multiplication.
 * </p>
 */
public class LPSolverProtocol4 implements ComputationBuilder<LPOutput> {

  public enum PivotRule {
    BLAND, DANZIG
  }

  ;

  private static final boolean debugLog = false;

  private final PivotRule pivotRule;
  private final LPTableau4 tableau;
  private final Matrix4<Computation<SInt>> updateMatrix;
  private final Computation<SInt> pivot;
  private final List<Computation<SInt>> initialBasis;
  private int identityHashCode;


  private int iterations = 0;
  private final int noVariables;
  private final int noConstraints;

  public LPSolverProtocol4(
      PivotRule pivotRule,
      LPTableau4 tableau,
      Matrix4<Computation<SInt>> updateMatrix,
      Computation<SInt> pivot,
      List<Computation<SInt>> initialBasis) {
    this.pivotRule = pivotRule;
    this.tableau = tableau;
    this.updateMatrix = updateMatrix;
    this.pivot = pivot;
    this.initialBasis = initialBasis;
    if (checkDimensions(tableau, updateMatrix)) {
      this.noVariables = tableau.getC().getWidth();
      this.noConstraints = tableau.getC().getHeight();
      identityHashCode = System.identityHashCode(this);
    } else {
      throw new MPCException("Dimensions of inputs does not match");
    }
  }

  public LPSolverProtocol4(
      LPTableau4 tableau,
      Matrix4<Computation<SInt>> updateMatrix,
      Computation<SInt> pivot,
      List<Computation<SInt>> initialBasis) {
    this(PivotRule.DANZIG, tableau, updateMatrix, pivot, initialBasis);
  }

  private boolean checkDimensions(LPTableau4 tableau, Matrix4<Computation<SInt>> updateMatrix) {
    int updateHeight = updateMatrix.getHeight();
    int updateWidth = updateMatrix.getWidth();
    int tableauHeight = tableau.getC().getHeight() + 1;
    return (updateHeight == updateWidth && updateHeight == tableauHeight);

  }


  @Override
  public Computation<LPOutput> build(SequentialProtocolBuilder builder) {
    Computation<SInt> zero = builder.numeric().known(BigInteger.ZERO);
    return builder.seq(seq -> {
      this.iterations = 0;
      List<BigInteger> enumeratedVariables = new ArrayList<>(noVariables);
      for (int i = 1; i <= noVariables; i++) {
        enumeratedVariables.add(BigInteger.valueOf(i));
      }

      LPState initialState = new LPState(
          BigInteger.ZERO, tableau, updateMatrix, null, pivot,
          enumeratedVariables, initialBasis, pivot);
      return () -> initialState;
    }).whileLoop(
        state -> !state.terminated(),
        (state, seq) -> {
          iterations++;
          if (debugLog) {
            debugInfo(seq, state);
          }
          return seq.seq((inner) -> {
            Reporter.info("LP Iterations=" + iterations + " solving " + identityHashCode);
            if (pivotRule == PivotRule.BLAND) {
              return blandPhaseOneProtocol(inner, state);
            } else {
              return phaseOneProtocol(inner, state, zero);
            }
          }).seq((phaseOneOutput, inner) -> {
            if (!phaseOneOutput.terminated()) {
              phaseTwoProtocol(inner, phaseOneOutput);
            }
            return phaseOneOutput;
          });
        }).seq((whileState, seq) ->
        () -> new LPOutput(whileState.tableau, whileState.updateMatrix, whileState.basis,
            whileState.pivot)
    );
  }

  /**
   * Creates a ProtocolProducer computing the second half of a simplex iteration.
   *
   * <p>
   * This finds the exiting variable index by finding the most constraining
   * constraint on the entering variable. Having the exiting variable index also
   * gives us the pivot. Having the entering and exiting indices and the pivot
   * allows us to compute the new update matrix for the next iteration.
   *
   * Additionally, having the entering and exiting variables we can update
   * the basis of the current solution.
   * </p>
   */
  private Computation<LPState> phaseTwoProtocol(
      SequentialProtocolBuilder builder,
      LPState state) {
    return builder.seq((seq) ->
        seq.createSequentialSub(
            new ExitingVariableProtocol4(
                state.tableau,
                state.updateMatrix,
                state.enteringIndex,
                state.basis))
    ).par((exitingVariable, seq) -> {
      state.pivot = exitingVariable.pivot.out();
      ArrayList<Computation<SInt>> exitingIndex = exitingVariable.exitingIndex;
      // Update Basis
      Computation<SInt> ent = seq.createAdvancedNumericBuilder()
          .openDot(state.enumeratedVariables, state.enteringIndex);
      return seq.createParallelSub((par) -> {
        ArrayList<Computation<SInt>> nextBasis = new ArrayList<>(noConstraints);
        for (int i = 0; i < noConstraints; i++) {
          nextBasis.add(
              par.createSequentialSub(
                  new ConditionalSelect(exitingIndex.get(i), ent, state.basis.get(i)))
          );
        }
        return () -> nextBasis;
      });
    }, (exitingVariable, seq) -> {
      return seq
          .createSequentialSub(new UpdateMatrixProtocol4(state.updateMatrix,
              exitingVariable.exitingIndex, exitingVariable.updateColumn, state.pivot,
              state.prevPivot
          ));
    }).seq((pair, seq) -> {
      List<Computation<SInt>> basis = pair.getFirst();
      state.updateMatrix = pair.getSecond();
      state.basis = basis;
//      // Copy the resulting new update matrix to overwrite the current
      state.prevPivot = state.pivot;
      return () -> state;
    });
  }

  /**
   * Creates a ProtocolProducer to compute the first half of a simplex iteration.
   * <p>
   * This finds the variable to enter the basis, based on the pivot rule of most
   * negative entry in the <i>F</i> vector. Also tests if no negative entry in
   * the <i>F</i> vector is present. If this is the case we should terminate
   * the simplex method.
   * </p>
   *
   * @return a protocol producer for the first half of a simplex iteration
   */
  private Computation<LPState> phaseOneProtocol(
      SequentialProtocolBuilder builder,
      LPState state,
      Computation<SInt> zero) {
    return builder.seq(
        // Compute potential entering variable index and corresponding value of
        // entry in F
        new EnteringVariableProtocol4(state.tableau, state.updateMatrix)
    ).seq((enteringAndMinimum, seq) -> {
      List<Computation<SInt>> entering = enteringAndMinimum.getFirst();
      SInt minimum = enteringAndMinimum.getSecond();
      // Check if the entry in F is non-negative
      Computation<SInt> positive = seq.comparison().compareLong(zero, minimum);
      state.terminationOut = seq.numeric().open(positive);
      state.enteringIndex = entering;
      return () -> state;
    });
  }

  /**
   * Creates a ProtocolProducer to compute the first half of a simplex iteration.
   * <p>
   * This finds the variable to enter the basis, based on Blands the pivot rule
   * using the first  negative entry in the <i>F</i> vector. Also tests if no
   * negative entry in the <i>F</i> vector is present. If this is the case we
   * should terminate the simplex method.
   * </p>
   *
   * @return a protocol producer for the first half of a simplex iteration
   */
  private Computation<LPState> blandPhaseOneProtocol(
      SequentialProtocolBuilder builder, LPState state) {
    return builder.seq(
        // Compute potential entering variable index and corresponding value of
        // entry in F
        new BlandEnteringVariableProtocol4(state.tableau, state.updateMatrix)
    ).seq((enteringAndMinimum, seq) -> {
      List<Computation<SInt>> entering = enteringAndMinimum.getFirst();
      SInt termination = enteringAndMinimum.getSecond();
      state.terminationOut = seq.numeric().open(termination);
      state.enteringIndex = entering;
      return () -> state;
    });
  }

  /**
   * Creates a protocolproducer that will print useful debugging information about the internal
   * state of the LPSolver. Designed to be called at the beginning of each iteration (after the
   * iteration count is incremented).
   *
   * @return a protocolproducer printing information.
   */
  private void debugInfo(SequentialProtocolBuilder builder, LPState state) {
    if (iterations == 1) {
      printInitialState(builder, state);
    } else {
      printState(builder, state);
    }
  }

  /**
   * Constructs a protocolproducer to print out the initial state of the LPSolver. NOTE: This
   * information is useful for debugging, but should not be reveal in regular usage.
   *
   * @return a protocolproducer printing information.
   */
  private void printInitialState(SequentialProtocolBuilder builder, LPState state) {
    BasicNumericFactory bnFactory = builder.getBasicNumericFactory();
    builder.append(new MarkerProtocolImpl("Initial Tableau [" + iterations + "]: "));
    tableau.toString(builder);
    builder
        .append(new OpenAndPrintProtocol("Basis [" + iterations + "]: ", state.basis, bnFactory));
    builder.append(new OpenAndPrintProtocol("Update Matrix [" + iterations + "]: ",
        updateMatrix.toArray(Computation::out, SInt[]::new, SInt[][]::new), bnFactory));
    builder.append(
        new OpenAndPrintProtocol("Pivot [" + iterations + "]: ", state.prevPivot.out(), bnFactory));
  }

  /**
   * Prints the current state of the LPSolver. NOTE: This information
   */
  private void printState(SequentialProtocolBuilder builder, LPState state) {
    BasicNumericFactory bnFactory = builder.getBasicNumericFactory();
    if (state.enteringIndex != null) {
      builder.append(
          new OpenAndPrintProtocol("Entering Variable [" + iterations + "]: ", state.enteringIndex,
              bnFactory));
    }
    builder
        .append(new OpenAndPrintProtocol("Basis [" + iterations + "]: ", state.basis, bnFactory));
    SInt[][] matrix = updateMatrix.toArray(Computation::out, SInt[]::new, SInt[][]::new);
    builder.append(new OpenAndPrintProtocol("Update Matrix [" + iterations + "]: ",
        matrix, bnFactory));
    builder.append(
        new OpenAndPrintProtocol("Pivot [" + iterations + "]: ", state.prevPivot.out(), bnFactory));
  }

  public static class LPOutput {

    public final LPTableau4 tableau;
    public final Matrix4<Computation<SInt>> updateMatrix;
    public final List<Computation<SInt>> basis;
    public final Computation<SInt> pivot;

    public LPOutput(LPTableau4 tableau,
        Matrix4<Computation<SInt>> updateMatrix,
        List<Computation<SInt>> basis, Computation<SInt> pivot) {
      this.tableau = tableau;
      this.updateMatrix = updateMatrix;
      this.basis = basis;
      this.pivot = pivot;
    }
  }

  private class LPState implements Computation<LPState> {

    public Computation<BigInteger> terminationOut;
    private LPTableau4 tableau;
    private Matrix4<Computation<SInt>> updateMatrix;
    public List<Computation<SInt>> enteringIndex;
    public Computation<SInt> pivot;
    public List<BigInteger> enumeratedVariables;
    public List<Computation<SInt>> basis;
    public Computation<SInt> prevPivot;

    public LPState(BigInteger terminationOut, LPTableau4 tableau,
        Matrix4<Computation<SInt>> updateMatrix, List<Computation<SInt>> enteringIndex,
        Computation<SInt> pivot,
        List<BigInteger> enumeratedVariables, List<Computation<SInt>> basis,
        Computation<SInt> prevPivot) {
      this.terminationOut = () -> terminationOut;
      this.tableau = tableau;
      this.updateMatrix = updateMatrix;
      this.enteringIndex = enteringIndex;
      this.pivot = pivot;
      this.enumeratedVariables = enumeratedVariables;
      this.basis = basis;
      this.prevPivot = prevPivot;
    }

    @Override
    public LPState out() {
      return this;
    }

    public boolean terminated() {
      return terminationOut.out().equals(BigInteger.ONE);
    }
  }
}
