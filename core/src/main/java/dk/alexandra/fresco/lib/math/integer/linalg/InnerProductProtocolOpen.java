package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
import java.util.ArrayList;
import java.util.List;

class InnerProductProtocolOpen implements ComputationBuilder<SInt> {

  private final List<OInt> aVector;
  private final List<Computation<SInt>> bVector;

  InnerProductProtocolOpen(List<OInt> aVector,
      List<Computation<SInt>> bVector) {
    this.aVector = aVector;
    this.bVector = bVector;
  }

  @Override
  public Computation<SInt> build(SequentialProtocolBuilder builder) {
    return builder
        .par(parallel -> {
          List<Computation<SInt>> result = new ArrayList<>(aVector.size());
          NumericBuilder numericBuilder = parallel.numeric();
          for (int i = 0; i < aVector.size(); i++) {
            OInt nextA = aVector.get(i);
            Computation<SInt> nextB = bVector.get(i);
            result.add(numericBuilder.mult(nextA, nextB));
          }
          return () -> result;
        })
        .seq((list, seq) ->
            new SumSIntList(list).build(seq)
        );
  }
}