package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.conditional.ConditionalSelect;
import dk.alexandra.fresco.lib.lp.LPSolver.LPOutput;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A protocol for solving LP problems using the Simplex method.
 *
 * <p>
 * We basically use the method of
 * <a href="http://fc09.ifca.ai/papers/69_Solving_linear_programs.pdf">Toft 2009</a>.
 *
 * We optimize this protocol by using the so called <i>Revised Simplex</i> method. I.e., instead of
 * updating the tableau it self, we keep track of much smaller update matrix representing changes to
 * the initial tableau. Do this only requires multiplication with a small sparse matrix, which can
 * be done more efficiently than general matrix multiplication.
 * </p>
 */
public class LPSolver implements Computation<LPOutput, ProtocolBuilderNumeric> {

  public enum PivotRule {
    BLAND, DANZIG
  }

  private final PivotRule pivotRule;
  private final LPTableau tableau;
  private final Matrix<DRes<SInt>> updateMatrix;
  private final DRes<SInt> pivot;
  private final List<DRes<SInt>> initialBasis;
  private int identityHashCode;


  private int iterations = 0;
  private final int noVariables;
  private final int noConstraints;

  private static Logger logger = LoggerFactory.getLogger(LPSolver.class);

  /**
   * Creates a new LPSolver. Note, we do not do full two-phase Simplex, so the initial state is 
   * assumed to be good.
   * 
   * @param pivotRule the pivot rule to apply
   * @param tableau the initial tableau, this will not be modified
   * @param updateMatrix the initial update matrix, will be modified to the current state
   * @param pivot the initial pivot, will be modified to reflect the  state
   * @param initialBasis the initial basis, will be modified to reflect the state
   */
  public LPSolver(PivotRule pivotRule, LPTableau tableau, Matrix<DRes<SInt>> updateMatrix,
      DRes<SInt> pivot, List<DRes<SInt>> initialBasis) {
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
      throw new IllegalArgumentException("Dimensions of inputs does not match");
    }
  }

  private boolean checkDimensions(LPTableau tableau, Matrix<DRes<SInt>> updateMatrix) {
    int updateHeight = updateMatrix.getHeight();
    int updateWidth = updateMatrix.getWidth();
    int tableauHeight = tableau.getC().getHeight() + 1;
    return (updateHeight == updateWidth && updateHeight == tableauHeight);

  }

  @Override
  public DRes<LPOutput> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<SInt> zero = builder.numeric().known(BigInteger.ZERO);
    return builder.seq(seq -> {
      this.iterations = 0;
      List<BigInteger> enumeratedVariables = new ArrayList<>(noVariables);
      for (int i = 1; i <= noVariables; i++) {
        enumeratedVariables.add(BigInteger.valueOf(i));
      }

      LpState initialState = new LpState(BigInteger.ZERO, tableau, updateMatrix, null, pivot,
          enumeratedVariables, initialBasis, pivot);
      return () -> initialState;
    }).whileLoop(state -> !state.terminated(), (seq, state) -> {
      iterations++;
      if (isDebug()) {
        debugInfo(seq, state);
      }
      return seq.seq((inner) -> {
        logger.info("LP Iterations=" + iterations + " solving " + identityHashCode);
        if (pivotRule == PivotRule.BLAND) {
          return blandPhaseOneProtocol(inner, state);
        } else {
          return phaseOneProtocol(inner, state, zero);
        }
      }).seq((inner, phaseOneOutput) -> {
        if (!phaseOneOutput.terminated()) {
          phaseTwoProtocol(inner, phaseOneOutput);
        }
        return phaseOneOutput;
      });
    }).seq((seq, whileState) -> () -> new LPOutput(whileState.tableau, whileState.updateMatrix,
        whileState.basis, whileState.pivot));
  }

  /**
   * Creates a ProtocolProducer computing the second half of a simplex iteration.
   *
   * <p>
   * This finds the exiting variable index by finding the most constraining constraint on the
   * entering variable. Having the exiting variable index also gives us the pivot. Having the
   * entering and exiting indices and the pivot allows us to compute the new update matrix for the
   * next iteration.
   *
   * Additionally, having the entering and exiting variables we can update the basis of the current
   * solution.
   * </p>
   */
  private DRes<LpState> phaseTwoProtocol(ProtocolBuilderNumeric builder, LpState state) {
    return builder.seq((seq) -> seq.seq(
        new ExitingVariable(state.tableau, state.updateMatrix, state.enteringIndex, state.basis)))
        .pairInPar((seq, exitingVariable) -> {
          state.pivot = exitingVariable.pivot;
          ArrayList<DRes<SInt>> exitingIndex = exitingVariable.exitingIndex;
          // Update Basis
          DRes<SInt> ent =
              seq.advancedNumeric().innerProductWithPublicPart(state.enumeratedVariables, 
                  state.enteringIndex);
          return seq.par((par) -> {
            ArrayList<DRes<SInt>> nextBasis = new ArrayList<>(noConstraints);
            for (int i = 0; i < noConstraints; i++) {
              nextBasis.add(
                  par.seq(new ConditionalSelect(exitingIndex.get(i), ent, state.basis.get(i))));
            }
            return () -> nextBasis;
          });
        }, (seq, exitingVariable) -> seq
            .seq(new UpdateMatrix(state.updateMatrix, exitingVariable.exitingIndex,
                exitingVariable.updateColumn, state.pivot, state.prevPivot)))
        .seq((seq, pair) -> {
          List<DRes<SInt>> basis = pair.getFirst();
          state.updateMatrix = pair.getSecond();
          state.basis = basis;
          // // Copy the resulting new update matrix to overwrite the current
          state.prevPivot = state.pivot;
          return state;
        });
  }

  /**
   * Creates a ProtocolProducer to compute the first half of a simplex iteration.
   * <p>
   * This finds the variable to enter the basis, based on the pivot rule of most negative entry in
   * the <i>F</i> vector. Also tests if no negative entry in the <i>F</i> vector is present. If this
   * is the case we should terminate the simplex method.
   * </p>
   *
   * @return a protocol producer for the first half of a simplex iteration
   */
  private DRes<LpState> phaseOneProtocol(ProtocolBuilderNumeric builder, LpState state,
      DRes<SInt> zero) {
    return builder
        .seq(
            // Compute potential entering variable index and corresponding value of
            // entry in F
            new EnteringVariable(state.tableau, state.updateMatrix))
        .seq((seq, enteringAndMinimum) -> {
          List<DRes<SInt>> entering = enteringAndMinimum.getFirst();
          SInt minimum = enteringAndMinimum.getSecond();
          // Check if the entry in F is non-negative
          DRes<SInt> positive = seq.comparison().compareLEQLong(zero, () -> minimum);
          state.terminationOut = seq.numeric().open(positive);
          state.enteringIndex = entering;
          return () -> state;
        });
  }

  /**
   * Creates a ProtocolProducer to compute the first half of a simplex iteration.
   * <p>
   * This finds the variable to enter the basis, based on Blands the pivot rule using the first
   * negative entry in the <i>F</i> vector. Also tests if no negative entry in the <i>F</i> vector
   * is present. If this is the case we should terminate the simplex method.
   * </p>
   *
   * @return a protocol producer for the first half of a simplex iteration
   */
  private DRes<LpState> blandPhaseOneProtocol(ProtocolBuilderNumeric builder, LpState state) {
    return builder
        .seq(
            // Compute potential entering variable index and corresponding value of
            // entry in F
            new BlandEnteringVariable(state.tableau, state.updateMatrix))
        .seq((seq, enteringAndMinimum) -> {
          List<DRes<SInt>> entering = enteringAndMinimum.getFirst();
          SInt termination = enteringAndMinimum.getSecond();
          state.terminationOut = seq.numeric().open(() -> termination);
          state.enteringIndex = entering;
          return () -> state;
        });
  }
  
  /**
   * Indicates if debug information should be opened and written. 
   * 
   * This is meant to be possible to override in debugging classes, but 
   * should always return false in non-debugging versions of this class.
   * @return true if this is a debugging class, false otherwise
   */
  protected boolean isDebug() {
    return false;
  }

  /**
   * Creates a protocolproducer that will print useful debugging information about the internal
   * state of the LPSolver. Designed to be called at the beginning of each iteration (after the
   * iteration count is incremented).
   *
   */
  private void debugInfo(ProtocolBuilderNumeric builder, LpState state) {
    if (iterations == 1) {
      printInitialState(builder, state);
    } else {
      printState(builder, state);
    }
  }

  /**
   * Prints out the initial state of the LPSolver to System.out. NOTE: This information is useful
   * for debugging, but should not be reveal in production environments.
   *
   */
  private void printInitialState(ProtocolBuilderNumeric builder, LpState state) {
    PrintStream stream = System.out;
    builder.debug().marker("Initial Tableau [" + iterations + "]: ", stream);
    state.tableau.debugInfo(builder, stream);
    builder.debug().openAndPrint("Basis [" + iterations + "]: ", state.basis, stream);
    builder.debug().openAndPrint("Update Matrix [" + iterations + "]: ", updateMatrix, stream);
    builder.debug().openAndPrint("Pivot [" + iterations + "]: ", state.prevPivot, stream);
  }

  /**
   * Prints the current state of the LPSolver to System.out. NOTE: This information is useful for
   * debugging, but should not be revealed in production environments.
   */
  private void printState(ProtocolBuilderNumeric builder, LpState state) {
    PrintStream stream = System.out;
    builder.debug().openAndPrint("Entering Variable [" + iterations + "]: ", state.enteringIndex,
        stream);
    builder.debug().openAndPrint("Basis [" + iterations + "]: ", state.basis, stream);
    builder.debug().openAndPrint("Update Matrix [" + iterations + "]: ", updateMatrix, stream);
    builder.debug().openAndPrint("Pivot [" + iterations + "]: ", state.prevPivot, stream);
  }

  public static class LPOutput {

    public final LPTableau tableau;
    public final Matrix<DRes<SInt>> updateMatrix;
    public final List<DRes<SInt>> basis;
    public final DRes<SInt> pivot;

    public LPOutput(LPTableau tableau, Matrix<DRes<SInt>> updateMatrix, List<DRes<SInt>> basis,
        DRes<SInt> pivot) {
      this.tableau = tableau;
      this.updateMatrix = updateMatrix;
      this.basis = basis;
      this.pivot = pivot;
    }
  }

  protected class LpState implements DRes<LpState> {

    public DRes<BigInteger> terminationOut;
    private LPTableau tableau;
    private Matrix<DRes<SInt>> updateMatrix;
    public List<DRes<SInt>> enteringIndex;
    public DRes<SInt> pivot;
    public List<BigInteger> enumeratedVariables;
    public List<DRes<SInt>> basis;
    public DRes<SInt> prevPivot;

    public LpState(BigInteger terminationOut, LPTableau tableau, Matrix<DRes<SInt>> updateMatrix,
        List<DRes<SInt>> enteringIndex, DRes<SInt> pivot, List<BigInteger> enumeratedVariables,
        List<DRes<SInt>> basis, DRes<SInt> prevPivot) {
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
    public LpState out() {
      return this;
    }

    public boolean terminated() {
      return terminationOut.out().equals(BigInteger.ONE);
    }
  }
}
