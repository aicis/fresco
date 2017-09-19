package dk.alexandra.fresco.lib.collections.permute;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.util.ArrayList;

public class PermuteRows implements Computation<Matrix<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<Matrix<DRes<SInt>>> values;
  private final int[] idxPerm;
  private final int permProviderPid;
  private final boolean isPermProvider;
  private final WaksmanUtils wutils;
  // not final as this will be set during protocol execution
  private Matrix<DRes<SInt>> cbits;

  /**
   * Constructs a new PermuteRows computation.
   * 
   * @param values rows to permute
   * @param idxPerm encodes the desired permutation by supplying for each index a new index
   * @param permProviderPid the ID of the party choosing permutation
   * @param isPermProvider flag that indicates if I provide permutation
   */
  public PermuteRows(DRes<Matrix<DRes<SInt>>> values, int[] idxPerm, int permProviderPid,
      boolean isPermProvider) {
    super();
    this.wutils = new WaksmanUtils();
    this.values = values;
    this.idxPerm = idxPerm;
    this.isPermProvider = isPermProvider;
    this.permProviderPid = permProviderPid;
  }

  private Matrix<DRes<SInt>> reroute(Matrix<DRes<SInt>> roundInputs, int numRows, int numCols,
      int numSwapperRows, int numSwapperCols, int colIdx) {
    // this will store the re-arranged result
    ArrayList<ArrayList<DRes<SInt>>> rearranged = new ArrayList<>(numRows);
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
  public DRes<Matrix<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder)
      throws UnsupportedOperationException {
    // sort of hacky: need to unwrap but will later used wrapped
    Matrix<DRes<SInt>> valuesOut = values.out();
    // determine dimensions of waksman network
    final int numSwapperRows = wutils.getNumRowsRequired(valuesOut.getHeight());
    final int numSwapperCols = wutils.getNumColsRequired(valuesOut.getHeight());
    // dimensions of row matrix
    final int numRows = valuesOut.getHeight();
    final int numCols = valuesOut.getWidth();
    // number of rounds will be number of swapper columns
    final int numRounds = numSwapperCols;

    if (numRounds == 0) {
      // in case of empty input, just return
      return values;
    }

    // throw if size is not power of 2
    if (!wutils.isPow2(numRows)) {
      throw new UnsupportedOperationException("input size must be power of 2");
    }

    // non-empty input, i.e., main protocol
    return builder.seq(seq -> {
      if (isPermProvider) {
        return seq.collections().closeMatrix(wutils.setControlBits(idxPerm), permProviderPid);
      } else {
        return seq.collections().closeMatrix(numSwapperRows, numSwapperCols, permProviderPid);
      }
    }).seq((seq, bits) -> {
      // set control bits
      cbits = bits;
      // initiate loop
      return new IterationState(0, values);
    }).whileLoop((state) -> state.round < numRounds - 1, (seq, state) -> {
      // apply swapper gates for this round
      DRes<Matrix<DRes<SInt>>> swapped =
          seq.collections().swapNeighborsIf(() -> cbits.getColumn(state.round), state.intermediate);
      // re-arrange values for next round (based solely on waksman network topology)
      // this is NOT input-dependent!
      return new IterationState(state.round + 1, () -> reroute(swapped.out(), numRows, numCols,
          numSwapperRows, numSwapperCols, state.round));
    }).seq((seq, state) -> {
      // Apply last column of swapper gates
      return seq.collections().swapNeighborsIf(() -> cbits.getColumn(state.round),
          state.intermediate);
    });
  }

  private static final class IterationState implements DRes<IterationState> {

    private final int round;
    private final DRes<Matrix<DRes<SInt>>> intermediate;

    private IterationState(int round, DRes<Matrix<DRes<SInt>>> intermediate) {
      this.round = round;
      this.intermediate = intermediate;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }

}
