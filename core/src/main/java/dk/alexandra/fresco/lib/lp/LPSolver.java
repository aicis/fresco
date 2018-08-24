package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
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
  private final int identityHashCode;


  private final int maxNumberOfIterations;
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
   * @param maxNumberOfIterations we might not terminate, the solver stops after this iteration
   */
  public LPSolver(PivotRule pivotRule, LPTableau tableau, Matrix<DRes<SInt>> updateMatrix,
      DRes<SInt> pivot, List<DRes<SInt>> initialBasis, int maxNumberOfIterations) {
    this.pivotRule = pivotRule;
    this.tableau = tableau;
    this.updateMatrix = updateMatrix;
    this.pivot = pivot;
    this.initialBasis = initialBasis;
    this.maxNumberOfIterations = maxNumberOfIterations;
    if (checkDimensions(tableau, updateMatrix)) {
      this.noVariables = tableau.getC().getWidth();
      this.noConstraints = tableau.getC().getHeight();
      this.identityHashCode = System.identityHashCode(this);
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
      List<BigInteger> enumeratedVariables = new ArrayList<>(noVariables);
      for (int i = 1; i <= noVariables; i++) {
        enumeratedVariables.add(BigInteger.valueOf(i));
      }

      LpState initialState = LpState.createInitialState(tableau, initialBasis, updateMatrix, pivot,
          enumeratedVariables, pivot);
      return () -> initialState;
    }).whileLoop(state -> !state.terminated(), (seq, state) -> {
      if (isDebug()) {
        debugInfo(seq, state);
      }
      return seq.seq((inner) -> {
        logger.info("LP Iterations=" + state.iteration + " solving " + identityHashCode);
        if (state.iteration >= maxNumberOfIterations) {
          logger.info("Aborting " + identityHashCode + " no solution found");
          return Pair.lazy(null, BigInteger.TEN);
        }
        if (pivotRule == PivotRule.BLAND) {
          return phaseOneBland(inner, state);
        } else {
          return phaseOneDanzig(inner, state, zero);
        }
      }).seq((inner, phaseOneOutput) -> {
        int phaseOneResult = phaseOneOutput.getSecond().intValue();
        if (phaseOneResult == 0) {
          if (isDebug()) {
            inner.debug()
                .openAndPrint("Entering Variable [" + state.iteration + "]: ",
                    phaseOneOutput.getFirst(), System.out);
          }
          return phaseTwoProtocol(inner, state, phaseOneOutput.getFirst());
        } else if (phaseOneResult == 1) {
          return state::createTerminationState;
        } else {
          // Abort, to many iterations
          return state::createAbortState;
        }
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
  private DRes<LpState> phaseTwoProtocol(ProtocolBuilderNumeric builder, LpState state,
      List<DRes<SInt>> entering) {
    return builder.seq((seq) -> seq.seq(
        new ExitingVariable(state.tableau, state.updateMatrix, entering, state.basis)))
        .pairInPar((seq, exitingVariable) -> {
          ArrayList<DRes<SInt>> exitingIndex = exitingVariable.exitingIndex;
          // Update Basis
          DRes<SInt> ent =
              seq.advancedNumeric().innerProductWithPublicPart(state.enumeratedVariables,
                  entering);
          return seq.par((par) -> {
            ArrayList<DRes<SInt>> nextBasis = new ArrayList<>(noConstraints);
            for (int i = 0; i < noConstraints; i++) {
              nextBasis.add(
                  par.seq(new ConditionalSelect(exitingIndex.get(i), ent, state.basis.get(i))));
            }
            return Pair.lazy(exitingVariable.pivot, nextBasis);
          });
        }, (seq, exitingVariable) -> seq
            .seq(new UpdateMatrix(state.updateMatrix, exitingVariable.exitingIndex,
                exitingVariable.updateColumn, exitingVariable.pivot, state.prevPivot)))
        .seq((seq, pair) -> {
          Matrix<DRes<SInt>> updateMatrix = pair.getSecond();
          List<DRes<SInt>> basis = pair.getFirst().getSecond();
          DRes<SInt> pivot = pair.getFirst().getFirst();
          return () -> state.createNextState(basis, updateMatrix, pivot);
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
   * @return a delayed result of the phaseOne computation of the first half of a simplex iteration
   */
  private DRes<Pair<List<DRes<SInt>>, BigInteger>> phaseOneDanzig(
      ProtocolBuilderNumeric builder, LpState state,
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
          DRes<BigInteger> terminationOut = seq.numeric().open(positive);
          return () -> new Pair<>(entering, terminationOut.out());
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
   * @return a delayed result of the phaseOne computation of the first half of a simplex iteration
   */
  private DRes<Pair<List<DRes<SInt>>, BigInteger>> phaseOneBland(
      ProtocolBuilderNumeric builder, LpState state) {
    return builder
        .seq(
            // Compute potential entering variable index and corresponding value of
            // entry in F
            new BlandEnteringVariable(state.tableau, state.updateMatrix))
        .seq((seq, enteringAndMinimum) -> {
          List<DRes<SInt>> entering = enteringAndMinimum.getFirst();
          SInt termination = enteringAndMinimum.getSecond();
          DRes<BigInteger> terminationOut = seq.numeric().open(() -> termination);
          return () -> new Pair<>(entering, terminationOut.out());
        });
  }

  /**
   * Indicates if debug information should be opened and written.
   *
   * This is meant to be possible to override in debugging classes, but should always return false
   * in non-debugging versions of this class.
   *
   * @return true if this is a debugging class, false otherwise
   */
  protected boolean isDebug() {
    return false;
  }

  /**
   * Creates a protocolproducer that will print useful debugging information about the internal
   * state of the LPSolver. Designed to be called at the beginning of each iteration (after the
   * iteration count is incremented).
   */
  private void debugInfo(ProtocolBuilderNumeric builder, LpState state) {
    if (state.iteration == 0) {
      printInitialState(builder, state);
    } else {
      printState(builder, state);
    }
  }

  /**
   * Prints out the initial state of the LPSolver to System.out. NOTE: This information is useful
   * for debugging, but should not be reveal in production environments.
   */
  private void printInitialState(ProtocolBuilderNumeric builder, LpState state) {
    PrintStream stream = System.out;
    int iterations = state.iteration;
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
    int iterations = state.iteration;
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

    public boolean isAborted() {
      return pivot == null;
    }
  }

  protected static class LpState {

    private final int iteration;
    private final boolean terminated;
    private final LPTableau tableau;
    private final Matrix<DRes<SInt>> updateMatrix;
    private final DRes<SInt> pivot;
    private final List<BigInteger> enumeratedVariables;
    private final List<DRes<SInt>> basis;
    private final DRes<SInt> prevPivot;

    private LpState(LPTableau tableau, List<DRes<SInt>> basis, Matrix<DRes<SInt>> updateMatrix,
        DRes<SInt> pivot, DRes<SInt> prevPivot, List<BigInteger> enumeratedVariables, int iteration,
        boolean terminated) {
      // Phase two protocol
      this.iteration = iteration;
      this.terminated = terminated;
      this.tableau = tableau;
      this.enumeratedVariables = enumeratedVariables;
      this.pivot = pivot;
      this.prevPivot = prevPivot;
      this.updateMatrix = updateMatrix;
      this.basis = basis;
    }

    public static LpState createInitialState(LPTableau tableau,
        List<DRes<SInt>> basis, Matrix<DRes<SInt>> updateMatrix,
        DRes<SInt> pivot, List<BigInteger> enumeratedVariables,
        DRes<SInt> prevPivot) {
      return new LpState(
          tableau, basis, updateMatrix, pivot, prevPivot, enumeratedVariables, 0, false
      );
    }

    public LpState createNextState(List<DRes<SInt>> basis, Matrix<DRes<SInt>> updateMatrix,
        DRes<SInt> pivot) {
      return new LpState(
          tableau, basis, updateMatrix, pivot, pivot, enumeratedVariables, iteration + 1, false);
    }

    public LpState createTerminationState() {
      return new LpState(tableau, basis, updateMatrix, pivot, pivot, null, iteration + 1, true);
    }

    public LpState createAbortState() {
      return new LpState(null, null, null, null, null, null, iteration + 1, true);
    }

    public boolean terminated() {
      return terminated;
    }
  }
}
