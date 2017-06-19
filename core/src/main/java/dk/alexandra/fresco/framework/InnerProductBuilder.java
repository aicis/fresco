package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public interface InnerProductBuilder<SIntT extends SInt> {

  Computation<SIntT> dot(List<Computation<SIntT>> aVector, List<Computation<SIntT>> bVector);

  Computation<SIntT> openDot(List<OInt> aVector, List<Computation<SIntT>> bVector);
}
