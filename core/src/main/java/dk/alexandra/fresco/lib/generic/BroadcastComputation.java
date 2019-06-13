package dk.alexandra.fresco.lib.generic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic active broadcast validation computation. <p>Uses generic native protocols {@link
 * InsecureBroadcastProtocol} and {@link BroadcastValidationProtocol}.</p>
 */
public class BroadcastComputation<BuilderT extends ProtocolBuilderImpl<BuilderT>> implements
    Computation<List<byte[]>, BuilderT> {

  private final List<byte[]> input;
  private final boolean runValidation;

  public BroadcastComputation(List<byte[]> input, boolean runValidation) {
    this.input = input;
    this.runValidation = runValidation;
  }

  public BroadcastComputation(byte[] input, boolean runValidation) {
    this(Collections.singletonList(input), runValidation);
  }

  public BroadcastComputation(byte[] input) {
    this(input, true);
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
      if (runValidation) {
        seq.append(new BroadcastValidationProtocol<>(toValidate));
      }
      return () -> toValidate;
    });
  }

}
