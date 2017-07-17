package dk.alexandra.fresco.demo.helpers;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary.SequentialBinaryBuilder;

public abstract class DemoBinaryApplication<Output> implements
    Application<Output, SequentialBinaryBuilder> {

  protected Computation<Output> output;

  public abstract ProtocolProducer prepareApplication(BuilderFactory factoryProducer);

  @Override
  public Computation<Output> prepareApplication(SequentialBinaryBuilder producer) {
    producer.append(prepareApplication(ProtocolBuilderHelper.getBinaryFactory(producer)));
    return this.output;
  }

  public Computation<Output> getOutput() {
    return output;
  }
}
