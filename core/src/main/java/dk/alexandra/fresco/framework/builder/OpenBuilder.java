package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;

public interface OpenBuilder {

  Computation<OInt> open(Computation<SInt> secretShare);
}
