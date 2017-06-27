package dk.alexandra.fresco.lib.conversion;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.RightShiftBuilder.RightShiftResult;
import dk.alexandra.fresco.framework.builder.FrescoFunction;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public class IntegerToBitsByShiftProtocolImpl4    implements FrescoFunction<List<SInt>> {

  private final Computation<SInt> input;
  private final int maxInputLength;

  public IntegerToBitsByShiftProtocolImpl4(Computation<SInt> input, int maxInputLength) {
    this.input = input;
    this.maxInputLength = maxInputLength;
  }

  @Override
  public Computation<List<SInt>> apply(SequentialProtocolBuilder builder) {
    Computation<RightShiftResult> rightShiftResult = builder.createRightShiftBuilder()
        .rightShiftWithRemainder(input, maxInputLength);
    return () -> rightShiftResult.out().getRemainder();
  }
}
