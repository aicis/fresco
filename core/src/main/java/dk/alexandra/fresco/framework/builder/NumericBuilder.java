package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;

public interface NumericBuilder<SIntT extends SInt> {

  Computation<SIntT> add(Computation<SIntT> a, Computation<SIntT> b);

  Computation<SIntT> sub(Computation<SIntT> a, Computation<SIntT> b);

  Computation<SIntT> mult(Computation<SIntT> a, Computation<SIntT> b);

}
