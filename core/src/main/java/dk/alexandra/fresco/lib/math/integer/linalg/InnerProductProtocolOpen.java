package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class InnerProductProtocolOpen<SIntT extends SInt> implements
    Function<SequentialProtocolBuilder<SIntT>, Computation<SIntT>> {

  private final List<OInt> aVector;
  private final List<Computation<SIntT>> bVector;

  InnerProductProtocolOpen(List<OInt> aVector,
      List<Computation<SIntT>> bVector) {
    this.aVector = aVector;
    this.bVector = bVector;
  }

  @Override
  public Computation<SIntT> apply(SequentialProtocolBuilder<SIntT> builder) {
    Computation<List<Computation<SIntT>>> products =
        builder.createParallelSubFactoryReturning(parallel -> {
          List<Computation<SIntT>> result = new ArrayList<>(aVector.size());
          NumericBuilder<SIntT> numericBuilder = parallel.numeric();
          for (int i = 0; i < aVector.size(); i++) {
            OInt nextA = aVector.get(i);
            Computation<SIntT> nextB = bVector.get(i);
            result.add(numericBuilder.mult(nextA, nextB));
          }
          return () -> result;
        });
    return builder.createSequentialSubFactoryReturning(new SumSIntList<>(products));
  }
}