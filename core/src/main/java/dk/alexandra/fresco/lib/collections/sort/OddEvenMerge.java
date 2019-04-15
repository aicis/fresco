package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
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

  private List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> numbers;
  private boolean mergePresortedHalves = false;

  OddEvenMerge(
      List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> unsortedNumbers) {
    super();
    this.numbers = unsortedNumbers;
  }

  OddEvenMerge(
      List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> unsortedNumbers,
      boolean mergePresortedHalves) {
    super();
    this.numbers = unsortedNumbers;
    this.mergePresortedHalves = mergePresortedHalves;
  }

  @Override
  public DRes<List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>>> buildComputation(
      ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      if (!this.mergePresortedHalves) {
        sort(0, numbers.size(), seq);
      } else {
        merge(0, numbers.size(), 1, seq);
      }
      return () -> numbers;
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
    builder.seq(seq -> seq.advancedBinary().keyedCompareAndSwap(numbers.get(i), numbers.get(j)))
        .seq((seq, res) -> {
          numbers.set(i, res.get(0));
          numbers.set(j, res.get(1));
          return null;
        });
  }

  private void merge(int first, int length, int step, ProtocolBuilderBinary builder) {
    int doubleStep = step * 2;
    if (doubleStep < length) {
      builder.seq((seq) -> {
        merge(first, length, doubleStep, seq);
        merge(first + step, length, doubleStep, seq);
        for (int idx = first + step; idx + step < first + length; idx += doubleStep) {
          compareAndSwapAtIndices(idx, idx + step, seq);
        }
        return null;
      });
    } else {
      compareAndSwapAtIndices(first, first + step, builder);
    }
  }
}
