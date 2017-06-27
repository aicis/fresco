package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

public class InnerProductProtocol44 implements ComputationBuilder<SInt> {

  private final List<Computation<SInt>> aVector;
  private final List<Computation<SInt>> bVector;

  public InnerProductProtocol44(
      List<Computation<SInt>> aVector,
      List<Computation<SInt>> bVector) {
    this.aVector = getRandomAccessList(aVector);
    this.bVector = getRandomAccessList(bVector);
  }

  private List<Computation<SInt>> getRandomAccessList(List<Computation<SInt>> aVector) {
    if (aVector instanceof RandomAccess) {
      return aVector;
    } else {
      return new ArrayList<>(aVector);
    }
  }

  @Override
  public Computation<SInt> build(SequentialProtocolBuilder builder) {
    return builder
        .par(parallel -> {
          List<Computation<SInt>> products = new ArrayList<>(aVector.size());
          NumericBuilder numericBuilder = parallel.numeric();
          for (int i = 0; i < aVector.size(); i++) {
            Computation<SInt> nextA = aVector.get(i);
            Computation<SInt> nextB = bVector.get(i);
            products.add(numericBuilder.mult(nextA, nextB));
          }
          return () -> products;
        })
        .seq((list, seq) ->
            new SumSIntList(list).build(seq)
        );
  }
}
