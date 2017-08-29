package dk.alexandra.fresco.lib.collections.permute;

import java.util.ArrayList;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.io.CloseMatrix;
import dk.alexandra.fresco.lib.conditional.ConditionalSwapNeighbors;

// TODO: for malicious security add check that control bits are in fact bits
public class PermuteRows implements ComputationBuilder<Matrix<Computation<SInt>>> {

  final private Matrix<Computation<SInt>> values;
  final private int[] idxPerm;
  final private int permProviderPid;
  final private boolean isPermProvider;
  final private WaksmanUtils wutils;
  // not final as this will be set during protocol execution
  private Matrix<Computation<SInt>> cbits;

  /**
   * To be called if party is not providing permutation.
   * 
   * @param values
   * @param permProviderPid
   */
  public PermuteRows(Matrix<Computation<SInt>> values, int permProviderPid) {
    this(values, new int[] {}, permProviderPid, false);
  }

  /**
   * To be called if party is providing permutation.
   * 
   * @param values
   * @param permProviderPid
   */
  public PermuteRows(Matrix<Computation<SInt>> values, int[] idxPerm, int permProviderPid) {
    this(values, idxPerm, permProviderPid, true);
  }

  private PermuteRows(Matrix<Computation<SInt>> values, int[] idxPerm, int permProviderPid,
      boolean isPermProvider) {
    super();
    this.values = values;
    this.idxPerm = idxPerm;
    this.isPermProvider = isPermProvider;
    this.permProviderPid = permProviderPid;
    this.wutils = new WaksmanUtils();
  }

  /**
   * Re-arranges the rows for the next round of swapper gates.
   * 
   * @param roundInputs
   * @param numRows
   * @param numCols
   * @param numSwapperRows
   * @param numSwapperCols
   * @param colIdx
   * @return
   */
  private Matrix<Computation<SInt>> reroute(Matrix<Computation<SInt>> roundInputs, int numRows,
      int numCols, int numSwapperRows, int numSwapperCols, int colIdx) {
    // this will store the re-arranged result
    ArrayList<ArrayList<Computation<SInt>>> rearranged = new ArrayList<>(numRows);
    // pre-initialize array list since we will be setting elements at indeces
    // as opposed to adding
    for (int x = 0; x < numRows; x++) {
      rearranged.add(null);
    }
    // determines if we are at a column before the center of the
    // network or after (fanning in vs fanning out)
    boolean inward = (colIdx < (int) numSwapperCols / 2);
    if (!inward) {
      colIdx = (numSwapperCols / 2) - colIdx % (numSwapperCols / 2) - 1;
    }
    int numPerms = (int) Math.pow(2, colIdx + 1);
    int elsPerPerm = (int) numRows / numPerms;
    for (int permPairIdx = 0; permPairIdx < numPerms - 1; permPairIdx += 2) {
      int topPermStart = permPairIdx * elsPerPerm;
      int bottomPermStart = (permPairIdx + 1) * elsPerPerm;
      int nextFreeTop = topPermStart;
      int nextFreeBottom = bottomPermStart;
      // re-arrange elements
      if (inward) {
        // we're haven't reached the center of the network yet
        // that is, we are fanning in
        for (int i = 0; i < elsPerPerm * 2 - 1; i += 2) {
          int inputIdx = topPermStart + i;
          rearranged.set(nextFreeTop++, roundInputs.getRow(inputIdx));
          rearranged.set(nextFreeBottom++, roundInputs.getRow(inputIdx + 1));
        }
      } else {
        // fanning out
        for (int i = 0; i < elsPerPerm * 2 - 1; i += 2) {
          int inputIdx = topPermStart + i;
          rearranged.set(inputIdx, roundInputs.getRow(nextFreeTop++));
          rearranged.set(inputIdx + 1, roundInputs.getRow(nextFreeBottom++));
        }
      }
    }
    return new Matrix<>(numRows, numCols, rearranged);
  }

  @Override
  public Computation<Matrix<Computation<SInt>>> build(SequentialNumericBuilder builder) {
    // determine dimensions of waksman network
    final int numSwapperRows = wutils.getNumRowsRequired(values.getHeight());
    final int numSwapperCols = wutils.getNumColsRequired(values.getHeight());
    // dimensions of row matrix
    final int numRows = values.getHeight();
    final int numCols = values.getWidth();
    // number of rounds will be number of swapper columns
    final int numRounds = numSwapperCols;

    if (numRounds == 0) {
      // in case of empty input, just return
      return () -> this.values;
    }

    // non-empty input, i.e., main protocol
    return builder.seq(seq -> {
      // determine control bits of permutation and input
      if (isPermProvider) {
        return seq
            .createParallelSub(new CloseMatrix(wutils.setControlBits(idxPerm), permProviderPid));
      } else {
        return seq
            .createParallelSub(new CloseMatrix(numSwapperRows, numSwapperCols, permProviderPid));
      }
    }).seq((bits, seq) -> {
      // set control bits
      cbits = bits;
      // initiate loop
      return new IterationState(0, () -> values);
    }).whileLoop((state) -> state.round < numRounds - 1, (state, seq) -> {
      // apply swapper gates for this round
      Computation<Matrix<Computation<SInt>>> swapped = seq.createParallelSub(
          new ConditionalSwapNeighbors(cbits.getColumn(state.round), state.intermediate.out()));
      // re-arrange values for next round (based solely on waksman network topology)
      // this is NOT input-dependent!
      return new IterationState(state.round + 1, () -> reroute(swapped.out(), numRows, numCols,
          numSwapperRows, numSwapperCols, state.round));
    }).seq((state, seq) -> {
      // Apply last column of swapper gates
      return seq.createParallelSub(
          new ConditionalSwapNeighbors(cbits.getColumn(state.round), state.intermediate.out()));
    });
  }

  private static final class IterationState implements Computation<IterationState> {

    private final int round;
    private final Computation<Matrix<Computation<SInt>>> intermediate;

    private IterationState(int round, Computation<Matrix<Computation<SInt>>> intermediate) {
      this.round = round;
      this.intermediate = intermediate;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }
}
