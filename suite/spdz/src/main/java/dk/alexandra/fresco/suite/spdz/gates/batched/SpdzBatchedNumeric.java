package dk.alexandra.fresco.suite.spdz.gates.batched;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.SpdzNumeric;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class SpdzBatchedNumeric extends SpdzNumeric {

  private Map<Integer, SpdzBatchedInputComputation> inputs = new HashMap<>();
  private SpdzBatchedMultiplication multiplications;
  private SpdzBatchedOutputToAll outputToAll;

  public SpdzBatchedNumeric(
      ProtocolBuilderNumeric protocolBuilder) {
    super(protocolBuilder);
  }

  @Override
  public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
    if (multiplications == null) {
      multiplications = new SpdzBatchedMultiplication();
      protocolBuilder.append(multiplications);
    }
    return multiplications.append(a, b);
  }

  @Override
  public DRes<SInt> input(BigInteger value, int inputParty) {
    if (!inputs.containsKey(inputParty)) {
      SpdzBatchedInputComputation input = new SpdzBatchedInputComputation(
          inputParty, protocolBuilder.getBasicNumericContext().getNoOfParties());
      inputs.put(inputParty, input);
      protocolBuilder.seq(input);
    }
    return inputs.get(inputParty).append(value);
  }

  @Override
  public DRes<BigInteger> open(DRes<SInt> secretShare) {
    if (outputToAll == null) {
      outputToAll = new SpdzBatchedOutputToAll();
      protocolBuilder.append(outputToAll);
    }
    return outputToAll.append(secretShare);
  }

}
