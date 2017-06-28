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
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ConditionalSelect;
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

  private final SInt zero;
  private final LPState initialState;
  private int identityHashCode;

  private BasicNumericFactory bnFactory;

  private static final int DEFAULT_BASIS_VALUE = 0;

  private int iterations = 0;
  private final int noVariables;
  private final int noConstraints;

  public LPSolverProtocol4(LPTableau4 tableau, Matrix4<Computation<SInt>> updateMatrix, SInt pivot,
      BasicNumericFactory bnFactory) {
    if (checkDimensions(tableau, updateMatrix)) {

      this.bnFactory = bnFactory;
      this.zero = bnFactory.getSInt(0);
      this.iterations = 0;

      this.noVariables = tableau.getC().getWidth();
      this.noConstraints = tableau.getC().getHeight();
      List<Computation<SInt>> basis = new ArrayList<>(noConstraints);
      for (int i = 0; i < noConstraints; i++) {
        basis.add(bnFactory.getSInt(DEFAULT_BASIS_VALUE));
      }
      List<BigInteger> enumeratedVariables = new ArrayList<>(noVariables);
      for (int i = 1; i <= noVariables; i++) {
        enumeratedVariables.add(BigInteger.valueOf(i));
      }

      this.initialState = new LPState(
          BigInteger.ZERO, tableau, updateMatrix, null, pivot,
          enumeratedVariables, basis, pivot);

    } else {
      throw new MPCException("Dimensions of inputs does not match");
    }
    identityHashCode = System.identityHashCode(this);
  }

  private boolean checkDimensions(LPTableau4 tableau, Matrix4<Computation<SInt>> updateMatrix) {
    int updateHeight = updateMatrix.getHeight();
    int updateWidth = updateMatrix.getWidth();
    int tableauHeight = tableau.getC().getHeight() + 1;
    return (updateHeight == updateWidth && updateHeight == tableauHeight);

  }


  @Override
  public Computation<LPOutput> build(SequentialProtocolBuilder builder) {
    return builder.seq(seq ->
        initialState
    ).whileLoop(
        state -> !state.terminated(),
        (state, seq) ->
            seq.seq((inner) -> {
              phaseOneProtocol(inner, state);
              return state;
            }).seq((phaseOneOutput, inner) -> {
              if (!phaseOneOutput.terminated()) {
                phaseTwoProtocol(inner, phaseOneOutput);
              }
              return phaseOneOutput;
            })
    ).seq((whileState, seq) ->
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
                state.enteringIndex
            ))
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
  private void phaseOneProtocol(
      SequentialProtocolBuilder builder,
      LPState state) {
    iterations++;
    Reporter.info("LP Iterations=" + iterations + " solving " +
        identityHashCode);
    builder.seq(
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
      return null;
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
  @SuppressWarnings("unused")
  private ProtocolProducer blandPhaseOneProtocol(LPState state) {
    //TODO Fix this for new API
/*    state.terminationOut = () -> bnFactory.getOInt();
    // Phase 1 - Finding the entering variable and outputting
    // whether or not the corresponding F value is positive (a positive
    // value indicating termination)
    state.enteringIndex = new SInt[noVariables];
    for (int i = 0; i < noVariables; i++) {
      state.enteringIndex[i] = bnFactory.getSInt();
    }

    SInt first = bnFactory.getSInt();
    ProtocolProducer blandEnter = new BlandEnteringVariableProtocol(state.tableau,
        state.updateMatrix,
        state.enteringIndex, first, lpFactory, bnFactory);

    Computation output = bnFactory.getOpenProtocol(first, state.terminationOut.out());
    return new SequentialProtocolProducer(blandEnter, output);*/
    return null;
  }

  public static class LPOutput {

    public final LPTableau4 tableau;
    public final Matrix4<Computation<SInt>> updateMatrix;
    public final List<Computation<SInt>> basis;
    public final SInt pivot;

    public LPOutput(LPTableau4 tableau,
        Matrix4<Computation<SInt>> updateMatrix,
        List<Computation<SInt>> basis, SInt pivot) {
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
    public SInt pivot;
    public List<BigInteger> enumeratedVariables;
    public List<Computation<SInt>> basis;
    public SInt prevPivot;

    public LPState(BigInteger terminationOut, LPTableau4 tableau,
        Matrix4<Computation<SInt>> updateMatrix, List<Computation<SInt>> enteringIndex, SInt pivot,
        List<BigInteger> enumeratedVariables, List<Computation<SInt>> basis, SInt prevPivot) {
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
