package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.AddSIntList;
import java.util.ArrayList;
import java.util.List;

public class InnerProductProtocol44<SIntT extends SInt> extends SimpleProtocolProducer
    implements Computation<SIntT> {

  private final List<Computation<SIntT>> aVector;
  private final List<Computation<SIntT>> bVector;
  private final BuilderFactoryNumeric<SIntT> factoryNumeric;
  private Computation<SIntT> result;

  InnerProductProtocol44(List<Computation<SIntT>> aVector,
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
          Computation<SIntT> nextA = aVector.get(i);
          Computation<SIntT> nextB = bVector.get(i);
          products.add(numericBuilder.mult(nextA, nextB));
        }
      });
      result = builder.createSequentialSubFactory(new AddSIntList<>(products));
    }).build();
  }
}
