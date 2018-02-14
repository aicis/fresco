package dk.alexandra.fresco.suite.marlin.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.BigUInt;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinAllBroadcastProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinBroadcastValidationProtocol;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MarlinMacCheckComputation<T extends BigUInt<T>> implements
    ComputationParallel<List<byte[]>, ProtocolBuilderNumeric> {

  private final List<byte[]> input;

  MarlinMacCheckComputation(List<byte[]> input) {
    this.input = input;
  }

  MarlinMacCheckComputation(byte[] input) {
    this(java.util.Collections.singletonList(input));
  }

  @Override
  public DRes<List<byte[]>> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<List<byte[]>>> broadcastValues = new LinkedList<>();
    for (byte[] singleInput : input) {
      broadcastValues.add(builder.append(new MarlinAllBroadcastProtocol<>(singleInput)));
    }
    return builder.seq(seq -> {
      List<byte[]> toValidate = broadcastValues.stream()
          .flatMap(broadcast -> broadcast.out().stream())
          .collect(Collectors.toList());
      DRes<Void> nothing = seq.append(new MarlinBroadcastValidationProtocol<>(toValidate));
      return () -> {
        nothing.out();
        return input;
      };
    });
  }

}
