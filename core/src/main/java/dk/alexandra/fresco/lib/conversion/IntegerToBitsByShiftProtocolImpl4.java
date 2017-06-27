package dk.alexandra.fresco.lib.conversion;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.AdvancedNumericBuilder.RightShiftResult;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public class IntegerToBitsByShiftProtocolImpl4 implements ComputationBuilder<List<SInt>> {

  private final Computation<SInt> input;
  private final int maxInputLength;

  public IntegerToBitsByShiftProtocolImpl4(Computation<SInt> input, int maxInputLength) {
    this.input = input;
    this.maxInputLength = maxInputLength;
  }

  @Override
  public Computation<List<SInt>> build(SequentialProtocolBuilder builder) {
    Computation<RightShiftResult> rightShiftResult = builder.createAdvancedNumericBuilder()
        .rightShiftWithRemainder(input, maxInputLength);
    return () -> rightShiftResult.out().getRemainder();
  }
}
