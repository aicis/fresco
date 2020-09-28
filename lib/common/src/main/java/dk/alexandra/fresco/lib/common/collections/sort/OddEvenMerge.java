package dk.alexandra.fresco.lib.common.collections.sort;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.common.math.AdvancedBinary;
import java.util.List;

/**
 * An implementation of the OddEvenMergeSortProtocol. We use Batcher's algorithm to sort a list of
 * boolean key/value pairs in descending order. This sorting algorithm runs in O(n(log(n))^2) time.
 *
 * You can set the mergePresortedHalves flag to `true` to merge a list where the sequence of the
 * first n / 2 elements is sorted in descending order and the sequence of the remaining n / 2
 * elements is also sorted in descending order. The protocol can then "merge" the two presorted
 * halves into a (descending order) sorted list in O(nlog(n)) time. Note, it is not currently known
 * whether it is possible to obliviously merge two lists in O(n) time.
 *
 * For now, this implementation only supports lists where the size is a power of 2 (i.e., 4, 8, 16,
 * etc.).
 */
public class OddEvenMerge implements
    Computation<List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>>, ProtocolBuilderBinary> {

  private final List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> keyValuePairs;
  private final boolean halvesPresorted;

  /**
   * Sorts a list of key-value pairs by key (first entry in pair).
   *
   * @param unsortedNumbers key-value pairs to sort
   * @param halvesPresorted true if first half and second half of tuples are already sorted in which
   * case we only need to merge
   */
  OddEvenMerge(
      List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> unsortedNumbers,
      boolean halvesPresorted) {
    this.keyValuePairs = unsortedNumbers;
    this.halvesPresorted = halvesPresorted;
  }

  /**
   * Default call to {@link #OddEvenMerge(List, boolean)}}. <p>By default we assume tuples are
   * completely unsorted.</p>
   */
  OddEvenMerge(
      List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> unsortedNumbers) {
    this(unsortedNumbers, false);
  }

  @Override
  public DRes<List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>>> buildComputation(
      ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      if (!this.halvesPresorted) {
        sort(0, keyValuePairs.size(), seq);
      } else {
        merge(0, keyValuePairs.size(), 1, seq);
      }
      return () -> keyValuePairs;
    });
  }

  private void sort(int i, int j, ProtocolBuilderBinary builder) {
    if (j > 1) {
      sort(i, j / 2, builder);
      sort(j / 2 + i, j / 2, builder);
      merge(i, j, 1, builder);
    }
  }

  private void compareAndSwapAtIndices(int i, int j, ProtocolBuilderBinary builder) {
    builder.seq(
        seq -> AdvancedBinary.using(seq).keyedCompareAndSwap(keyValuePairs.get(i), keyValuePairs.get(j)))
        .seq((seq, res) -> {
          keyValuePairs.set(i, res.get(0));
          keyValuePairs.set(j, res.get(1));
          return null;
        });
  }

  private void merge(int first, int length, int step, ProtocolBuilderBinary builder) {
    int doubleStep = step * 2;
    if (doubleStep < length) {
      builder.seq((seq) -> {
        merge(first, length, doubleStep, seq);
        merge(first + step, length, doubleStep, seq);
        return seq.par(par -> {
          for (int idx = first + step; idx + step < first + length; idx += doubleStep) {
            compareAndSwapAtIndices(idx, idx + step, par);
          }
          return null;
        });
      });
    } else {
      compareAndSwapAtIndices(first, first + step, builder);
    }
  }
}
