package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class InnerProductProtocol44<SIntT extends SInt> implements
    Function<SequentialProtocolBuilder<SIntT>, Computation<SIntT>> {

  private final List<Computation<SIntT>> aVector;
  private final List<Computation<SIntT>> bVector;

  InnerProductProtocol44(
      List<Computation<SIntT>> aVector,
      List<Computation<SIntT>> bVector) {
    this.aVector = aVector;
    this.bVector = bVector;
  }

  @Override
  public Computation<SIntT> apply(SequentialProtocolBuilder<SIntT> builder) {
    return builder
        .par(parallel -> {
          List<Computation<SIntT>> products = new ArrayList<>(aVector.size());
          NumericBuilder<SIntT> numericBuilder = parallel.numeric();
          for (int i = 0; i < aVector.size(); i++) {
            Computation<SIntT> nextA = aVector.get(i);
            Computation<SIntT> nextB = bVector.get(i);
            products.add(numericBuilder.mult(nextA, nextB));
          }
          return () -> products;
        })
        .seq(new SumSIntList<>());
  }
}
