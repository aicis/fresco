package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;

public interface OpenBuilder<SIntT extends SInt> {

  Computation<OInt> open(Computation<SIntT> secretShare);
}
