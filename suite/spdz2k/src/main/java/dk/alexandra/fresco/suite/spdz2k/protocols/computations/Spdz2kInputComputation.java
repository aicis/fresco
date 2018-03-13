package dk.alexandra.fresco.suite.spdz2k.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.BroadcastValidationProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kInputOnlyProtocol;

/**
 * Native computation for inputting private data. <p>Consists of native protocols {@link
 * Spdz2kInputOnlyProtocol} and {@link BroadcastValidationProtocol}. The first returns this party's
 * share of the input along with the bytes of the masked input. The second step runs a broadcast
 * validation of the bytes of the masked input (if more than two parties are carrying out the
 * computation).</p>
 */
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
    DRes<Pair<DRes<SInt>, byte[]>> shareAndMaskBytes = builder
        .append(new Spdz2kInputOnlyProtocol<>(input, inputPartyId));
    return builder.seq(seq -> {
      Pair<DRes<SInt>, byte[]> unwrapped = shareAndMaskBytes.out();
      seq.append(new BroadcastValidationProtocol<>(unwrapped.getSecond()));
      return unwrapped.getFirst();
    });
  }

}
