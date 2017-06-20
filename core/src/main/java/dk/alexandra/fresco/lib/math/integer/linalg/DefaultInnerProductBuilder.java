package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.InnerProductBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public class DefaultInnerProductBuilder<SIntT extends SInt> implements InnerProductBuilder<SIntT> {

  private final ProtocolBuilder<SIntT> builder;

  public DefaultInnerProductBuilder(ProtocolBuilder<SIntT> builder) {
    this.builder = builder;
  }

  @Override
  public Computation<SIntT> dot(List<Computation<SIntT>> aVector,
      List<Computation<SIntT>> bVector) {
    return builder
        .createSequentialSubFactoryReturning(new InnerProductProtocol44<>(aVector, bVector));
  }

  @Override
  public Computation<SIntT> openDot(List<OInt> aVector, List<Computation<SIntT>> bVector) {
    return builder
        .createSequentialSubFactoryReturning(new InnerProductProtocolOpen<>(aVector, bVector));
  }
}
