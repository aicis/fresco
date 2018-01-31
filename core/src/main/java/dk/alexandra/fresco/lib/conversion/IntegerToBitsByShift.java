package dk.alexandra.fresco.lib.conversion;

import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RightShiftResult;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Converts a number to its bit representation by shifting the maximum
 * bit-length of the number times.
 */
public class IntegerToBitsByShift implements Computation<List<SInt>, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int maxInputLength;
  private final DelayedComputation<List<SInt>> result = new DelayedComputation<>();

  public IntegerToBitsByShift(DRes<SInt> input, int maxInputLength) {
    this.input = input;
    this.maxInputLength = maxInputLength;
  }

  @Override
  public DRes<List<SInt>> buildComputation(ProtocolBuilderNumeric builder) {
    doIteration(builder, input, maxInputLength, new ArrayList<SInt>(maxInputLength));
    return result;
  }

  private void doIteration(ProtocolBuilderNumeric producer, DRes<SInt> input, int shifts,
      List<SInt> remainders) {

    if (shifts > 0) {
      DRes<RightShiftResult> iteration = producer
          .seq((seq) -> seq.advancedNumeric().rightShiftWithRemainder(input));
      producer.createIteration((seq) -> {
        remainders.add(iteration.out().getRemainder());
        doIteration(seq, iteration.out().getResult(), shifts - 1, remainders);
      });
    } else {
      result.setComputation(() -> remainders);
    }
  }

  private static class DelayedComputation<R> implements DRes<R> {

    private DRes<R> closure;

    public void setComputation(DRes<R> computation) {
      this.closure = computation;
    }

    @Override
    public R out() {
      return closure.out();
    }
  }
}
