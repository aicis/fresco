package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.AddSIntList;
import java.util.ArrayList;
import java.util.List;

class InnerProductProtocolOpen<SIntT extends SInt> extends SimpleProtocolProducer
    implements Computation<SIntT> {

  private final List<OInt> aVector;
  private final List<Computation<SIntT>> bVector;
  private final BuilderFactoryNumeric<SIntT> factoryNumeric;
  private Computation<SIntT> result;

  InnerProductProtocolOpen(List<OInt> aVector,
      List<Computation<SIntT>> bVector,
      BuilderFactoryNumeric<SIntT> factoryNumeric) {
    this.aVector = aVector;
    this.bVector = bVector;
    this.factoryNumeric = factoryNumeric;
  }

  @Override
  public SIntT out() {
    return result.out();
  }

  @Override
  protected ProtocolProducer initializeProtocolProducer() {
    return ProtocolBuilder.createRoot(factoryNumeric, builder -> {
      List<Computation<SIntT>> products = new ArrayList<>(aVector.size());
      builder.createParallelSubFactory(parallel -> {
        NumericBuilder<SIntT> numericBuilder = parallel.createNumericBuilder();
        for (int i = 0; i < aVector.size(); i++) {
          OInt nextA = aVector.get(i);
          Computation<SIntT> nextB = bVector.get(i);
          products.add(numericBuilder.mult(nextA, nextB));
        }
      });
      result = builder.createSequentialSubFactory(new AddSIntList<>(products));
    }).build();
  }
}