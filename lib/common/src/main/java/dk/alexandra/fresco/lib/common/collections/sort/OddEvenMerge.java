package dk.alexandra.fresco.lib.common.collections.sort;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderImpl;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;
import java.util.function.BiFunction;

/**
 * An implementation of the OddEvenMergeProtocol. We use batchers algorithm.
 */
public class OddEvenMerge<KeyT, ValueT,
    ConditionT, BuilderT extends ProtocolBuilderImpl> implements
    Computation<List<Pair<KeyT, List<ValueT>>>, BuilderT> {

  private final BiFunction<Pair<KeyT, List<ValueT>>, Pair<KeyT, List<ValueT>>, KeyedCompareAndSwap<KeyT, ValueT, ConditionT, BuilderT>> compareAndSwapProvider;
  private final List<Pair<KeyT, List<ValueT>>> numbers;

  private OddEvenMerge(
      List<Pair<KeyT, List<ValueT>>> unsortedNumbers,
      BiFunction<Pair<KeyT, List<ValueT>>, Pair<KeyT, List<ValueT>>, KeyedCompareAndSwap<KeyT, ValueT, ConditionT, BuilderT>> compareAndSwapProvider) {
    super();
    this.compareAndSwapProvider = compareAndSwapProvider;

    // Verify that the payloads all have the same size, to avoid leaking info based on this
    unsortedNumbers.forEach(current -> {
      if (current.getSecond().size() != unsortedNumbers.get(0).getSecond().size()) {
        throw new UnsupportedOperationException(
            "All payload lists must have equal length to avoid leaking info");
      }
    });
    this.numbers = unsortedNumbers;
  }

  public static OddEvenMerge<List<DRes<SBool>>, DRes<SBool>, DRes<SBool>, ProtocolBuilderBinary> binary(
      List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> unsortedNumbers) {
    return new OddEvenMerge<>(unsortedNumbers, KeyedCompareAndSwap::binary);
  }

  public static OddEvenMerge<DRes<SInt>, DRes<SInt>, DRes<SInt>, ProtocolBuilderNumeric> numeric(
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> unsortedNumbers) {
    return new OddEvenMerge<>(unsortedNumbers, KeyedCompareAndSwap::numeric);
  }

  @Override
  public DRes<List<Pair<KeyT, List<ValueT>>>> buildComputation(
      BuilderT builder) {
    int t = (int) Math.ceil(Math.log(numbers.size()) / Math.log(2.0));
    int p0 = (1 << t);
    return builder.seq(seq -> {
      iterativeSort(p0, (BuilderT) seq);
      return () -> numbers;
    });
  }

  private void iterativeSort(int p0, BuilderT builder) {
    builder.seq(seq -> new Iteration(p0, 0, p0, p0))
        .whileLoop((iteration) -> ((Iteration) iteration).p > 0, (seq, iteration) -> {
          ((BuilderT) seq).seq(innerSeq -> () -> iteration)
              .whileLoop((state) -> ((Iteration) state).d > 0, (whileSeq, state) -> {
                final Iteration s = (Iteration) state;
                ((BuilderT) whileSeq).par((par) -> {
                  for (int i = 0; i < numbers.size() - s.d; i++) {
                    if ((i & s.p) == s.r) {
                      compareAndSwapAtIndices(i, i + s.d, (BuilderT) par);
                    }
                  }
                  return null;
                });
                return new Iteration(s.q >> 1, s.p, s.q - s.p, s.p);
              });
          final Iteration s = (Iteration) iteration;
          return new Iteration(p0, 0, s.p >> 1, s.p >> 1);
        });
  }

  private void compareAndSwapAtIndices(int i, int j, BuilderT builder) {
    builder
        .par(par -> compareAndSwapProvider.apply(numbers.get(i), numbers.get(j)).buildComputation(
            (BuilderT) par)).par((par, res) -> {
      List<Pair<KeyT, List<ValueT>>> r = (List<Pair<KeyT, List<ValueT>>>) res;
      numbers.set(i, r.get(0));
      numbers.set(j, r.get(1));
      return null;
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

}
