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
 * An implementation of the OddEvenMergeSortProtocol. We use Batcher's algorithm to sort a list of
 * key/value pairs in descending order. This sorting algorithm runs in O(n(log(n))^2) time. The
 * algorithm is not stable, so there is no guarantees as to how ties are located relatively in the
 * sorted list.
 *
 * @param <KeyT>       The type of keys
 * @param <ValueT>     The type of elements in the payload which will be a list of elements.
 * @param <ConditionT> The type of element representing a boolean value, so it should have a
 *                     canonical interpretation as a boolean 0/1 value. Used as condition in
 *                     conditional swap.
 * @param <BuilderT>   The type of {@link ProtocolBuilderImpl} used.
 */
public class OddEvenMerge<KeyT, ValueT,
    ConditionT, BuilderT extends ProtocolBuilderImpl<BuilderT>> implements
    Computation<List<Pair<KeyT, List<ValueT>>>, BuilderT> {

  private final BiFunction<Pair<KeyT, List<ValueT>>, Pair<KeyT, List<ValueT>>, KeyedCompareAndSwap<KeyT, ValueT, ConditionT, BuilderT>> compareAndSwapProvider;
  private final List<Pair<KeyT, List<ValueT>>> numbers;

  private OddEvenMerge(
      List<Pair<KeyT, List<ValueT>>> unsortedNumbers,
      BiFunction<Pair<KeyT, List<ValueT>>, Pair<KeyT, List<ValueT>>, KeyedCompareAndSwap<KeyT, ValueT, ConditionT, BuilderT>> compareAndSwapProvider) {
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
    return new OddEvenMerge<List<DRes<SBool>>, DRes<SBool>, DRes<SBool>, ProtocolBuilderBinary>(
        unsortedNumbers, KeyedCompareAndSwap::binary);
  }

  public static OddEvenMerge<DRes<SInt>, DRes<SInt>, DRes<SInt>, ProtocolBuilderNumeric> numeric(
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> unsortedNumbers) {
    return new OddEvenMerge<DRes<SInt>, DRes<SInt>, DRes<SInt>, ProtocolBuilderNumeric>(
        unsortedNumbers, KeyedCompareAndSwap::numeric);
  }

  @Override
  public DRes<List<Pair<KeyT, List<ValueT>>>> buildComputation(
      BuilderT builder) {
    int t = (int) Math.ceil(Math.log(numbers.size()) / Math.log(2.0));
    int p0 = (1 << t);
    return builder.seq(seq -> {
      iterativeSort(p0, seq);
      return () -> numbers;
    });
  }

  private void iterativeSort(int p0, BuilderT builder) {
    builder.seq(seq -> new Iteration(p0, 0, p0, p0))
        .whileLoop((iteration) -> iteration.p > 0, (seq, iteration) -> {
          seq.seq(innerSeq -> () -> iteration)
              .whileLoop((state) -> state.d > 0, (whileSeq, state) -> {
                final Iteration s = state;
                whileSeq.par((par) -> {
                  for (int i = 0; i < numbers.size() - s.d; i++) {
                    if ((i & s.p) == s.r) {
                      compareAndSwapAtIndices(i, i + s.d, par);
                    }
                  }
                  return null;
                });
                return new Iteration(s.q >> 1, s.p, s.q - s.p, s.p);
              });
          return new Iteration(p0, 0, iteration.p >> 1, iteration.p >> 1);
        });
  }

  private void compareAndSwapAtIndices(int i, int j, BuilderT builder) {
    builder
        .par(par -> compareAndSwapProvider.apply(numbers.get(i), numbers.get(j)).buildComputation(
            par)).par((par, res) -> {
      numbers.set(i, res.get(0));
      numbers.set(j, res.get(1));
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
