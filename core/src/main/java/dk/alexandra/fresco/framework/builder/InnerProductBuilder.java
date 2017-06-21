package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public interface InnerProductBuilder {

  Computation<SInt> dot(List<Computation<SInt>> aVector, List<Computation<SInt>> bVector);

  Computation<SInt> openDot(List<OInt> aVector, List<Computation<SInt>> bVector);
}
