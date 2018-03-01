package dk.alexandra.fresco.lib.conversion;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RightShiftResult;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts a number to its bit representation by shifting the maximum
 * bit-length of the number times.
 */
public class IntegerToBitsByShift implements Computation<List<SInt>, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int maxInputLength;

  public IntegerToBitsByShift(DRes<SInt> input, int maxInputLength) {
    this.input = input;
    this.maxInputLength = maxInputLength;
  }

  @Override
  public DRes<List<SInt>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder
        .seq((ignored) -> () -> new State(input, maxInputLength))
        .whileLoop(
            (state) -> state.shifts > 0,
            (seq, state) -> {
              DRes<RightShiftResult> remainder =
                  seq.advancedNumeric().rightShiftWithRemainder(state.currentInput);
              return () ->
                  state.createNext(remainder.out().getResult(), remainder.out().getRemainder());
            })
        .seq((seq, state) ->
            () -> state.remainders
                .stream()
                .map(DRes::out)
                .collect(Collectors.toList()));
  }

  private class State {

    private final DRes<SInt> currentInput;
    private final int shifts;
    private final List<DRes<SInt>> remainders;

    public State(DRes<SInt> currentInput, int shifts) {
      this.currentInput = currentInput;
      this.shifts = shifts;
      this.remainders = new ArrayList<>();
    }

    public State createNext(DRes<SInt> value, DRes<SInt> nextRemainder) {
      State state = new State(value, shifts - 1);
      state.remainders.addAll(remainders);
      state.remainders.add(nextRemainder);
      return state;
    }
  }
}
