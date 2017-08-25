package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.BuildStep.BuildStepSequential;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ProtocolBuilderImpl<BuilderT extends ProtocolBuilderImpl<BuilderT>>
    implements ProtocolBuilder {

  private final boolean parallel;
  private List<ProtocolEntity> protocols;
  private BuilderFactory<BuilderT> factory;

  protected ProtocolBuilderImpl(
      BuilderFactory<BuilderT> factory,
      boolean parallel) {
    this.parallel = parallel;
    this.protocols = new LinkedList<>();
    this.factory = factory;
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested parallel protocol
   * producer inserted into the original protocol producer.
   *
   * @param function of the protocol producer - will be lazy evaluated
   */
  public <R> Computation<R> createParallelSub(
      ComputationBuilder<R, BuilderT> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.build(builder)),
        () -> factory.createParallel());
    return result;
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested sequential protocol
   * producer inserted into the original protocol producer.
   *
   * @param function creation of the protocol producer - will be lazy evaluated
   */
  public <R> Computation<R> createSequentialSub(
      ComputationBuilder<R, BuilderT> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.build(builder)),
        () -> factory.createSequential());
    return result;
  }

  /**
   * Creates another protocol builder based on the supplied consumer. This method re-creates the
   * builder based on a sequential protocol producer inserted into this original protocol producer
   * as a child.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public <T extends Consumer<BuilderT>> void createIteration(T consumer) {
    addConsumer(consumer, () -> factory.createSequential());
  }

  protected <T extends ProtocolBuilderImpl> void addConsumer(Consumer<T> consumer,
      Supplier<T> supplier) {
    createAndAppend(new LazyProtocolProducerDecorator(() -> {
      T builder = supplier.get();
      consumer.accept(builder);
      return builder.build();
    }));
  }

  private void createAndAppend(ProtocolProducer producer) {
    ProtocolEntity protocolEntity = new ProtocolEntity(producer);
    if (protocols == null) {
      throw new IllegalStateException("Cannot build this twice, it has all ready been constructed");
    }
    protocols.add(protocolEntity);
  }

  /**
   * Appends a concrete, native protocol to the list of producers - useful for the native protocol
   * factories that needs to be builders.
   *
   * @param nativeProtocol the native protocol to add
   * @param <T> the result type of the native protocol
   * @return a computation that resolves to the result of the native protocol once evaluated
   */
  public <T> Computation<T> append(NativeProtocol<T, ?> nativeProtocol) {
    SingleProtocolProducer<T> producer = new SingleProtocolProducer<>(nativeProtocol);
    createAndAppend(producer);
    return producer;
  }

  // This will go away and should not be used - users should recode their applications to
  // use closures
  @Deprecated
  public <T extends ProtocolProducer> T append(T protocolProducer) {
    createAndAppend(protocolProducer);
    return protocolProducer;
  }

  /**
   * Building the actual protocol producer. Implementors decide which producer to create.
   *
   * @return the protocol producer that has been build
   */
  public ProtocolProducer build() {
    if (parallel) {
      ParallelProtocolProducer parallelProtocolProducer = new ParallelProtocolProducer();
      addEntities(parallelProtocolProducer);
      return parallelProtocolProducer;
    } else {
      SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
      addEntities(sequentialProtocolProducer);
      return sequentialProtocolProducer;
    }
  }

  private void addEntities(ProtocolProducerCollection producerCollection) {
    for (ProtocolEntity protocolEntity : protocols) {
      producerCollection.append(protocolEntity.protocolProducer);
    }
    protocols = null;
  }

  public <R>
  BuildStep<BuilderT, R, Void>
  seq(ComputationBuilder<R, BuilderT> function) {
    BuildStep<BuilderT, R, Void> builder =
        new BuildStepSequential<>((ignored, inner) -> function.build(inner));
    createAndAppend(
        new LazyProtocolProducerDecorator(() -> builder.createProducer(null, factory)));
    return builder;
  }

  public <R> BuildStep<BuilderT, R, Void> par(
      ComputationBuilder<R, BuilderT> f) {
    BuildStep<BuilderT, R, Void> builder =
        new BuildStep.BuildStepParallel<>((ignored, inner) -> f.build(inner));
    createAndAppend(
        new LazyProtocolProducerDecorator(() -> builder.createProducer(null, factory)));
    return builder;
  }

  // Pending rewrite of the last remaining applications
  @Deprecated
  public BuilderFactory getFactory() {
    return factory;
  }

  private static class ProtocolEntity {

    final ProtocolProducer protocolProducer;

    private ProtocolEntity(ProtocolProducer producer) {
      protocolProducer = producer;
    }
  }
}
