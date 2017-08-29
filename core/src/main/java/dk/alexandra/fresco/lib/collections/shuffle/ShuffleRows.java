package dk.alexandra.fresco.lib.collections.shuffle;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.permute.PermuteRows;

public class ShuffleRows implements ComputationBuilder<Matrix<Computation<SInt>>> {

  final private Matrix<Computation<SInt>> values;
  final private Random rnd;
  final private int pid;
  final private int[] pids;

  public ShuffleRows(Matrix<Computation<SInt>> values, Random rnd, int pid, int[] pids) {
    super();
    this.values = values;
    this.rnd = rnd;
    this.pid = pid;
    this.pids = pids;
  }

  public ShuffleRows(Matrix<Computation<SInt>> values, int pid, int[] pids) {
    this(values, new SecureRandom(), pid, pids);
  }

  private int[] getIdxPerm(int n) {
    List<Integer> indeces = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      indeces.add(i);
    }
    Collections.shuffle(indeces, rnd);
    int[] idxPerm = new int[indeces.size()];
    for (int i = 0; i < indeces.size(); i++) {
      idxPerm[indeces.get(i)] = i;
    }
    return idxPerm;
  }

  @Override
  public Computation<Matrix<Computation<SInt>>> build(SequentialNumericBuilder builder) {
    /*
     * There is a round for each party in pids. Each party chooses a random permutation (of indexes)
     * and applies it to values using PermuteRows.
     */
    if (values.getHeight() == 0) {
      return () -> values;
    }
    return builder.seq((seq) -> {
      return new IterationState(0, () -> values);
    }).whileLoop((state) -> state.round < pids.length, (state, seq) -> {
      int thisRoundPid = pids[state.round];
      if (pid == thisRoundPid) {
        Computation<Matrix<Computation<SInt>>> permuted = seq.createSequentialSub(
            new PermuteRows(values, getIdxPerm(values.getHeight()), thisRoundPid));
        return new IterationState(state.round + 1, permuted);
      } else {
        Computation<Matrix<Computation<SInt>>> permuted =
            seq.createSequentialSub(new PermuteRows(values, thisRoundPid));
        return new IterationState(state.round + 1, permuted);
      }
    }).seq((state, seq) -> {
      return state.intermediate;
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
