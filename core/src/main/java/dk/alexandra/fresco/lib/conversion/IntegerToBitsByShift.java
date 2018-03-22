package dk.alexandra.fresco.lib.conversion;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RightShiftResult;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a number to its bit representation by shifting the maximum bit-length of the number
 * times.
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
              return () -> state.createNext(remainder.out());
            })
        .seq((seq, state) -> () -> state.remainders);
  }

  private class State {

    private final DRes<SInt> currentInput;
    private final int shifts;
    private final List<SInt> remainders;

    State(DRes<SInt> currentInput, int shifts) {
      this.currentInput = currentInput;
      this.shifts = shifts;
      this.remainders = new ArrayList<>();
    }

    State createNext(RightShiftResult value) {
      State state = new State(value.getResult(), shifts - 1);
      state.remainders.addAll(remainders);
      state.remainders.add(value.getRemainder());
      return state;
    }
  }
}
