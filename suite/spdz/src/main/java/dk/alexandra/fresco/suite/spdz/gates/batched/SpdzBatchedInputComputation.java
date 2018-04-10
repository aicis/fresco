package dk.alexandra.fresco.suite.spdz.gates.batched;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.broadcast.BroadcastValidationProtocol;
import java.math.BigInteger;

/**
 * Native batched computation for inputting private data. <p>Consists of native batched protocols
 * {@link SpdzBatchedInputOnly} and {@link ...}. The first returns this party's share of the input
 * along with the bytes of the masked input. The second step runs a broadcast validation of the
 * bytes of the masked input (if more than two parties are carrying out the computation).</p>
 */
public class SpdzBatchedInputComputation implements Computation<Void, ProtocolBuilderNumeric> {

  private final SpdzBatchedInputOnly inputOnly;

  public SpdzBatchedInputComputation(int inputPartyId, int noOfParties) {
    inputOnly = new SpdzBatchedInputOnly(inputPartyId, noOfParties > 2);
  }

  public DRes<SInt> append(BigInteger input) {
    return inputOnly.append(input);
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    if (builder.getBasicNumericContext().getNoOfParties() <= 2) {
      // no need for broadcast validation in two-party case
      builder.append(inputOnly);
      return null;
    } else {
      DRes<byte[]> toValidate = builder.append(inputOnly);
      return builder.seq(seq -> {
        seq.append(new BroadcastValidationProtocol<>(toValidate.out()));
        return null;
      });
    }
  }

}
