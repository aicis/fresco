package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.List;

/**
 * An implementation of the OddEvenMergeProtocol. We use batchers algorithm. For now, this
 * implementation only supports lists where the size is a power of 2. (i.e. 4, 8, 16 etc.)
 *
 */
public class OddEvenMerge implements
    Computation<List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>>, ProtocolBuilderBinary> {

  private List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> numbers;

  public OddEvenMerge(
      List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>> unsortedNumbers) {
    super();
    this.numbers = unsortedNumbers;
  }

  @Override
  public DRes<List<Pair<List<DRes<SBool>>, List<DRes<SBool>>>>> buildComputation(
      ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      sort(0, numbers.size() - 1, seq);
      return () -> numbers;
    });
  }

  private void sort(int i, int j, ProtocolBuilderBinary builder) {
    if (j - i > 1) {
      sort(i, j / 2, builder);
      sort(j / 2 + 1, j, builder);
      merge(i, (j - i + 1), 1, builder);
    } else {
      compareAndSwapAtIndices(i, j, builder);
    }
  }

  private void compareAndSwapAtIndices(int i, int j, ProtocolBuilderBinary builder) {
    builder.seq(seq -> {
      return seq.advancedBinary().keyedCompareAndSwap(numbers.get(i), numbers.get(j));
    }).seq((seq, res) -> {
      numbers.set(i, res.get(0));
      numbers.set(j, res.get(1));
      return null;
    });
  }

  private void merge(int first, int length, int step, ProtocolBuilderBinary builder) {
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
