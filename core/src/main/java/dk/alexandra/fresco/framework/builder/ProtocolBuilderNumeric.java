package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Central class for building protocols that are based on numeric protocols.
 */
public abstract class ProtocolBuilderNumeric implements ProtocolBuilder {

  private BasicNumericFactory basicNumericFactory;
  private List<ProtocolBuilderNumeric.ProtocolEntity> protocols;
  private BuilderFactoryNumeric factory;
  private NumericBuilder numericBuilder;
  private ComparisonBuilder comparison;
  private AdvancedNumericBuilder advancedNumeric;
  private UtilityBuilder utilityBuilder;

  private ProtocolBuilderNumeric(BuilderFactoryNumeric factory) {
    this.factory = factory;
    this.basicNumericFactory = factory.getBasicNumericFactory();
    this.protocols = new LinkedList<>();
  }

  public BasicNumericFactory getBasicNumericFactory() {
    return basicNumericFactory;
  }

  /**
   * Creates a root for this builder - should be applied when construcing protocol producers from an
   * {@link Application}.
   *
   * @param factory the protocol factory to get native protocols and composite builders from
   * @param consumer the root of the protocol producer
   * @return a sequential protocol builder that can create the protocol producer
   */
  public static SequentialNumericBuilder createApplicationRoot(BuilderFactoryNumeric factory,
      Consumer<SequentialNumericBuilder> consumer) {
    SequentialNumericBuilder builder = new SequentialNumericBuilder(factory);
    builder.addConsumer(consumer, () -> new SequentialNumericBuilder(factory));
    return builder;
  }

  public static SequentialNumericBuilder createApplicationRoot(
      BuilderFactoryNumeric builderFactoryNumeric) {
    return new SequentialNumericBuilder(builderFactoryNumeric);
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested parallel protocol
   * producer inserted into the original protocol producer.
   *
   * @param function of the protocol producer - will be lazy evaluated
   */
  public <R> Computation<R> createParallelSub(ComputationBuilderParallel<R> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.build(builder)),
        () -> new ParallelNumericBuilder(factory));
    return result;
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested sequential protocol
   * producer inserted into the original protocol producer.
   *
   * @param function creation of the protocol producer - will be lazy evaluated
   */
  public <R> Computation<R> createSequentialSub(ComputationBuilder<R> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.build(builder)),
        () -> new SequentialNumericBuilder(factory));
    return result;
  }

  /**
   * Creates another protocol builder based on the supplied consumer. This method re-creates the
   * builder based on a sequential protocol producer inserted into this original protocol producer
   * as a child.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public <T extends Consumer<SequentialNumericBuilder>> void createIteration(T consumer) {
    addConsumer(consumer, () -> new SequentialNumericBuilder(factory));
  }

  <T extends ProtocolBuilderNumeric> void addConsumer(Consumer<T> consumer, Supplier<T> supplier) {
    createAndAppend(new LazyProtocolProducerDecorator(() -> {
      T builder = supplier.get();
      consumer.accept(builder);
      return builder.build();
    }));
  }

  ProtocolBuilderNumeric.ProtocolEntity createAndAppend(ProtocolProducer producer) {
    ProtocolBuilderNumeric.ProtocolEntity protocolEntity =
        new ProtocolBuilderNumeric.ProtocolEntity(producer);
    if (protocols == null) {
      throw new IllegalStateException("Cannot build this twice, it has all ready been constructed");
    }
    protocols.add(protocolEntity);
    return protocolEntity;
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
  public abstract ProtocolProducer build();

  void addEntities(ProtocolProducerCollection producerCollection) {
    for (ProtocolBuilderNumeric.ProtocolEntity protocolEntity : protocols) {
      producerCollection.append(protocolEntity.protocolProducer);
    }
    protocols = null;
  }

  /**
   * Creates a numeric builder for this instance - i.e. this intended producer.
   *
   * @return the numeric builder.
   */
  public NumericBuilder numeric() {
    if (numericBuilder == null) {
      numericBuilder = factory.createNumericBuilder(this);
    }
    return numericBuilder;
  }

  /**
   * Creates a comparison builder for this instance - i.e. this intended producer.
   *
   * @return the comparison builder.
   */
  public ComparisonBuilder comparison() {
    if (comparison == null) {
      comparison = factory.createComparison(this);
    }
    return comparison;
  }

  public AdvancedNumericBuilder advancedNumeric() {
    if (advancedNumeric == null) {
      advancedNumeric = factory.createAdvancedNumeric(this);
    }
    return advancedNumeric;
  }

  public UtilityBuilder utility() {
    if (utilityBuilder == null) {
      utilityBuilder = factory.createUtilityBuilder(this);
    }
    return utilityBuilder;
  }

  public MiscOIntGenerators getBigIntegerHelper() {
    return factory.getBigIntegerHelper();
  }

  // Pending rewrite of the last remaining applications
  @Deprecated
  public BuilderFactoryNumeric getFactory() {
    return factory;
  }

  private static class ProtocolEntity {

    final ProtocolProducer protocolProducer;

    private ProtocolEntity(ProtocolProducer producer) {
      protocolProducer = producer;
    }
  }

  /**
   * A specific instance of the protocol builder that produces a sequential producer.
   */
  public static class SequentialNumericBuilder extends ProtocolBuilderNumeric {

    SequentialNumericBuilder(BuilderFactoryNumeric factory) {
      super(factory);
    }

    @Override
    public ProtocolProducer build() {
      SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
      addEntities(sequentialProtocolProducer);
      return sequentialProtocolProducer;
    }


    public <R> BuildStep<SequentialNumericBuilder, R, Void> seq(ComputationBuilder<R> function) {
      BuildStep<SequentialNumericBuilder, R, Void> builder =
          new BuildStepSequential<>((ignored, inner) -> function.build(inner));
      createAndAppend(
          new LazyProtocolProducerDecorator(() -> builder.createProducer(null, getFactory())));
      return builder;
    }

    public <R> BuildStep<ParallelNumericBuilder, R, Void> par(ComputationBuilderParallel<R> f) {
      BuildStep<ParallelNumericBuilder, R, Void> builder =
          new BuildStepParallel<>((ignored, inner) -> f.build(inner));
      createAndAppend(
          new LazyProtocolProducerDecorator(() -> builder.createProducer(null, getFactory())));
      return builder;
    }
  }

  /**
   * A specific instance of the protocol builder that produces a parallel producer.
   */
  public static class ParallelNumericBuilder extends ProtocolBuilderNumeric {

    ParallelNumericBuilder(BuilderFactoryNumeric factory) {
      super(factory);
    }

    @Override
    public ProtocolProducer build() {
      ParallelProtocolProducer parallelProtocolProducer = new ParallelProtocolProducer();
      addEntities(parallelProtocolProducer);
      return parallelProtocolProducer;
    }
  }
}
