package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;

public class OpenBuilder<SIntT extends SInt> {

  public Computation<OInt> open(Computation<SIntT> result) {
    throw new RuntimeException("Error");
  }
}
