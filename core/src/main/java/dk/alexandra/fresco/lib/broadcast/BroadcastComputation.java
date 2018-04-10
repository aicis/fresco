package dk.alexandra.fresco.lib.broadcast;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic active broadcast validation computation. <p>Uses generic native protocols {@link
 * InsecureBroadcastProtocol} and {@link BroadcastValidationProtocol}.</p>
 */
public class BroadcastComputation<BuilderT extends ProtocolBuilderImpl<BuilderT>> implements
    Computation<List<byte[]>, BuilderT> {

  private final List<byte[]> input;

  public BroadcastComputation(List<byte[]> input) {
    this.input = input;
  }

  public BroadcastComputation(byte[] input) {
    this(java.util.Collections.singletonList(input));
  }

  @Override
  public DRes<List<byte[]>> buildComputation(BuilderT builder) {
    return builder.par(par -> {
      List<DRes<List<byte[]>>> broadcastValues = new ArrayList<>();
      for (byte[] singleInput : input) {
        broadcastValues.add(par.append(new InsecureBroadcastProtocol<>(singleInput)));
      }
      return () -> broadcastValues;
    }).seq((seq, lst) -> {
      List<byte[]> toValidate = lst.stream()
          .flatMap(broadcast -> broadcast.out().stream())
          .collect(Collectors.toList());
      seq.append(new BroadcastValidationProtocol<>(toValidate));
      return () -> toValidate;
    });
  }

}
