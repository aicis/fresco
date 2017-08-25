package dk.alexandra.fresco.lib.conversion;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder.RightShiftResult;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public class IntegerToBitsByShift implements
    ComputationBuilder<List<SInt>, SequentialNumericBuilder> {

  private final Computation<SInt> input;
  private final int maxInputLength;

  public IntegerToBitsByShift(Computation<SInt> input, int maxInputLength) {
    this.input = input;
    this.maxInputLength = maxInputLength;
  }

  @Override
  public Computation<List<SInt>> build(SequentialNumericBuilder builder) {
    Computation<RightShiftResult> rightShiftResult = builder.advancedNumeric()
        .rightShiftWithRemainder(input, maxInputLength);
    return () -> rightShiftResult.out().getRemainder();
  }
}
