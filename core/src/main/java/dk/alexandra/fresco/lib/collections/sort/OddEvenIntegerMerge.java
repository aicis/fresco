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
    return builder.seq(seq -> {
      sort(0, numbers.size(), seq);
      return () -> numbers;
    });
  }

  private void sort(int i, int length, ProtocolBuilderNumeric builder) {
    if (length > 1) {
      sort(i, length / 2, builder);
      sort(i + length / 2, length / 2, builder);
      merge(i, length, 1, builder);
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

  private void merge(int first, int length, int step, ProtocolBuilderNumeric builder) {
    int doubleStep = step * 2;
    if (length > 2) {
      builder.seq((seq) -> {
        int newLength = length / 2;
        merge(first, newLength, doubleStep, seq);
        merge(first + step, length - newLength, doubleStep, seq);
        for (int i = 1; i < length - 2; i += 2) {
          int low = first + i * step;
          int high = low + step;
          compareAndSwapAtIndices(low, high, seq);
        }
        return null;
      });
    } else {
      compareAndSwapAtIndices(first, first + step, builder);
    }
  }
}
