package dk.alexandra.fresco.suite.spdz.gates.batched;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Native batched computation for inputting private data. <p>Consists of native batched protocols
 * {@link SpdzBatchedInputOnly} and {@link ...}. The first returns this
 * party's share of the input along with the bytes of the masked input. The second step runs a
 * broadcast validation of the bytes of the masked input (if more than two parties are carrying out
 * the computation).</p>
 */
public class SpdzBatchedInputComputation implements Computation<Void, ProtocolBuilderNumeric> {

  private final Map<Integer, SpdzBatchedInputOnly> inputOnly;

  public SpdzBatchedInputComputation(int noOfParties) {
    inputOnly = new HashMap<>();
    for (int partyId = 1; partyId <= noOfParties; partyId++) {
      inputOnly.put(partyId, new SpdzBatchedInputOnly(partyId));
    }
  }

  public DRes<SInt> append(BigInteger input, int inputPartyId) {
    return inputOnly.get(inputPartyId).append(input);
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      for (SpdzBatchedInputOnly input : inputOnly.values()) {
        par.append(input);
      }
      return null;
    });
  }

}
