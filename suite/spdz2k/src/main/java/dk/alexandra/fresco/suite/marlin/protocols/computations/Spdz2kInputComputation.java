package dk.alexandra.fresco.suite.marlin.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.protocols.natives.BroadcastValidationProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.natives.Spdz2kInputOnlyProtocol;

public class Spdz2kInputComputation<PlainT extends CompUInt<?, ?, PlainT>> implements
    Computation<SInt, ProtocolBuilderNumeric> {

  private final PlainT input;
  private final int inputPartyId;

  public Spdz2kInputComputation(PlainT input, int inputPartyId) {
    this.inputPartyId = inputPartyId;
    this.input = input;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<Pair<DRes<SInt>, byte[]>> pair = builder
        .append(new Spdz2kInputOnlyProtocol<>(input, inputPartyId));
    return builder.seq(seq -> {
      Pair<DRes<SInt>, byte[]> unwrapped = pair.out();
      DRes<Void> nothing = seq
          .append(new BroadcastValidationProtocol<>(unwrapped.getSecond()));
      return () -> {
        nothing.out();
        return unwrapped.getFirst().out();
      };
    });
  }

}
