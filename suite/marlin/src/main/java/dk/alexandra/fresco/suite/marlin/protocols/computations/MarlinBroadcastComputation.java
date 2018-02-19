package dk.alexandra.fresco.suite.marlin.protocols.computations;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.suite.marlin.datatypes.CompositeUInt;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinAllBroadcastProtocol;
import dk.alexandra.fresco.suite.marlin.protocols.natives.MarlinBroadcastValidationProtocol;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MarlinBroadcastComputation<T extends CompositeUInt<T>> implements
    Computation<List<byte[]>, ProtocolBuilderNumeric> {

  private final List<byte[]> input;

  MarlinBroadcastComputation(List<byte[]> input) {
    this.input = input;
  }

  MarlinBroadcastComputation(byte[] input) {
    this(java.util.Collections.singletonList(input));
  }

  @Override
  public DRes<List<byte[]>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<List<byte[]>>> broadcastValues = new LinkedList<>();
      for (byte[] singleInput : input) {
        broadcastValues.add(par.append(new MarlinAllBroadcastProtocol<>(singleInput)));
      }
      return () -> broadcastValues;
    }).seq((seq, lst) -> {
      List<byte[]> toValidate = lst.stream()
          .flatMap(broadcast -> broadcast.out().stream())
          .collect(Collectors.toList());
      DRes<Void> nothing = seq.append(new MarlinBroadcastValidationProtocol<>(toValidate));
      return () -> {
        nothing.out();
        return toValidate;
      };
    });
  }

}
