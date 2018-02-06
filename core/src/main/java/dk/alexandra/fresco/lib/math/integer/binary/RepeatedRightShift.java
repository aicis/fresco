package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RightShiftResult;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * Shifts a number to the right an amount of times.
 */
public class RepeatedRightShift implements
    Computation<RightShiftResult, ProtocolBuilderNumeric> {

  private final int shifts;
  private final boolean calculateRemainders;
  // Input
  private final DRes<SInt> input;
  private final DelayedComputation<RightShiftResult> result = new DelayedComputation<>();


  /**
   * @param input The input.
   * @param calculateRemainders true to also calculate remainder. If false remainders in result
   *     will
   *     be null.
   */
  public RepeatedRightShift(
      DRes<SInt> input,
      int shifts,
      boolean calculateRemainders) {
    if (shifts < 0) {
      throw new IllegalArgumentException("Number of shifts must be positive");
    }
    this.input = input;
    this.shifts = shifts;
    this.calculateRemainders = calculateRemainders;
  }

  @Override
  public DRes<RightShiftResult> buildComputation(ProtocolBuilderNumeric sequential) {
    if (calculateRemainders) {
      doIterationWithRemainder(sequential, input, shifts, new ArrayList<>(shifts));
    } else {
      doIteration(sequential, input, shifts);
    }
    return result;
  }

  private void doIteration(ProtocolBuilderNumeric iterationBuilder,
      DRes<SInt> input, int shifts) {
    if (shifts > 0) {
      DRes<SInt> iteration =
          iterationBuilder.seq((builder) -> builder.advancedNumeric().rightShift(input));
      iterationBuilder.createIteration((builder) -> doIteration(builder, iteration, shifts - 1));
    } else {
      result.setComputation(() -> new RightShiftResult(input.out(), null));
    }
  }

  private void doIterationWithRemainder(ProtocolBuilderNumeric iterationBuilder,
      DRes<SInt> input, int shifts, List<SInt> remainders) {
    if (shifts > 0) {
      DRes<RightShiftResult> iteration =
          iterationBuilder
              .seq((builder) -> builder.advancedNumeric().rightShiftWithRemainder(input));
      iterationBuilder.createIteration((builder) -> {
        RightShiftResult out = iteration.out();
        remainders.add(out.getRemainder().get(0));
        doIterationWithRemainder(builder, out::getResult, shifts - 1, remainders);
      });
    } else {
      result.setComputation(() -> new RightShiftResult(input.out(), remainders));
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