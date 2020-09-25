package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

/**
 * An implementation of the OddEvenMergeProtocol. We use batchers algorithm. For now, this
 * implementation only supports lists where the size is a power of 2. (i.e. 4, 8, 16 etc.)
 *
 */
public class OddEvenIntegerMerge implements
    Computation<List<Pair<DRes<SInt>, List<DRes<SInt>>>>, ProtocolBuilderNumeric> {

  private List<Pair<DRes<SInt>, List<DRes<SInt>>>> numbers;

  public OddEvenIntegerMerge(
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> unsortedNumbers) {
    super();
    // Verify that the input is a two power
    if (Integer.bitCount(unsortedNumbers.size()) != 1) {
      throw new UnsupportedOperationException("Implementation only supports computation on list of a two-power size");
    }
    // Verify that the payloads all have the same size, to avoid leaking info based on this
    unsortedNumbers.forEach( current -> {
      if (current.getSecond().size() != unsortedNumbers.get(0).getSecond().size()) {
        throw new UnsupportedOperationException("All payload lists must have equal length to avoid leaking info");
      }
    });
    this.numbers = unsortedNumbers;
  }

  @Override
  public DRes<List<Pair<DRes<SInt>, List<DRes<SInt>>>>> buildComputation(
      ProtocolBuilderNumeric builder) {
    // This is sufficient since we know the size is a two-power
    int t = (int) Math.ceil(Math.log(numbers.size())/Math.log(2.0));//Integer.highestOneBit(numbers.size());
    int p0 = (1 << t);
    return builder.seq( (seq) -> {
      iterativeSort(p0, seq);
      return () -> numbers;
    });
  }

  private void iterativeSort(int p0, ProtocolBuilderNumeric builder) {
    builder.seq((seq) -> {
      return new Iteration(p0, 0, p0, p0);
    }).whileLoop((iteration) -> iteration.p > 0, (seq, iteration) -> {
      seq.seq(innerSeq -> {
        return () -> iteration;
      }).whileLoop((state) -> state.d > 0, (whileSeq, state) -> {
        whileSeq.par((par) -> {
          for (int i = 0; i < numbers.size() - state.d; i++) {
            if ((i & state.p) == state.r) {
              compareAndSwapAtIndices(i, i + state.d, par);
            }
          }
          return null;
        });
        return new Iteration(state.q >> 1, state.p, state.q - state.p, state.p);
      });
      return new Iteration(p0, 0, iteration.p >> 1, iteration.p >> 1);
    });
  }

  private static final class Iteration implements DRes<Iteration> {
    final int q;
    final int r;
    final int d;
    final int p;

    private Iteration(int q, int r, int d, int p) {
      this.q = q;
      this.r = r;
      this.d = d;
      this.p = p;
    }
    @Override
    public Iteration out() {
      return this;
    }
  }

  private void compareAndSwapAtIndices(int i, int j, ProtocolBuilderNumeric builder) {
    builder.par(par -> {
      return par.advancedNumeric().keyedCompareAndSwap(numbers.get(i), numbers.get(j));
    }).par((par, res) -> {
      numbers.set(i, res.get(0));
      numbers.set(j, res.get(1));
      return null;
    });
  }
}