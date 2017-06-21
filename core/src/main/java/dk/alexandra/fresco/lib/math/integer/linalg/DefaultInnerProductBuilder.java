package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.InnerProductBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public class DefaultInnerProductBuilder implements InnerProductBuilder {

  private final ProtocolBuilder builder;

  public DefaultInnerProductBuilder(ProtocolBuilder builder) {
    this.builder = builder;
  }

  @Override
  public Computation<SInt> dot(List<Computation<SInt>> aVector,
      List<Computation<SInt>> bVector) {
    return builder
        .createSequentialSubFactoryReturning(new InnerProductProtocol44(aVector, bVector));
  }

  @Override
  public Computation<SInt> openDot(List<OInt> aVector, List<Computation<SInt>> bVector) {
    return builder
        .createSequentialSubFactoryReturning(new InnerProductProtocolOpen(aVector, bVector));
  }
}
