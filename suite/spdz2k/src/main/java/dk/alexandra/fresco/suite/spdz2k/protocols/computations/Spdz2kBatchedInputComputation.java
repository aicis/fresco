package dk.alexandra.fresco.suite.spdz2k.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.broadcast.BroadcastValidationProtocol;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.batched.Spdz2kBatchedInputOnly;

/**
 * Batched variant of {@link Spdz2kInputComputation}.
 */
public class Spdz2kBatchedInputComputation<PlainT extends CompUInt<?, ?, PlainT>> implements
    Computation<Void, ProtocolBuilderNumeric> {

  private final Spdz2kBatchedInputOnly<PlainT> inputOnly;

  public Spdz2kBatchedInputComputation(int inputPartyId, int noOfParties) {
    inputOnly = new Spdz2kBatchedInputOnly<>(inputPartyId, noOfParties > 2);
  }

  public DRes<SInt> append(PlainT input) {
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
