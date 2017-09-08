package dk.alexandra.fresco.lib.conversion;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RightShiftResult;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public class IntegerToBitsByShift implements
    Computation<List<SInt>, ProtocolBuilderNumeric> {

  private final DRes<SInt> input;
  private final int maxInputLength;

  public IntegerToBitsByShift(DRes<SInt> input, int maxInputLength) {
    this.input = input;
    this.maxInputLength = maxInputLength;
  }

  @Override
  public DRes<List<SInt>> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<RightShiftResult> rightShiftResult = builder.advancedNumeric()
        .rightShiftWithRemainder(input, maxInputLength);
    return () -> rightShiftResult.out().getRemainder();
  }
}
