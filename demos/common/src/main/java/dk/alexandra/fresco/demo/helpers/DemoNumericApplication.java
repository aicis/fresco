package dk.alexandra.fresco.demo.helpers;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;

public abstract class DemoNumericApplication<Output> implements
    Application<Output, ProtocolBuilderNumeric> {

  protected Computation<Output> output;

  @Deprecated
  public abstract ProtocolProducer prepareApplication(BuilderFactory factoryProducer);

  @Override
  public Computation<Output> prepareApplication(ProtocolBuilderNumeric producer) {
    producer.append(prepareApplication(ProtocolBuilderHelper.getNumericFactory(producer)));
    return this.output;
  }

  public Computation<Output> getOutput() {
    return output;
  }
}
