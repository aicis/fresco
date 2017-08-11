package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.DelayedComputation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ProtocolBuilderBinary implements ProtocolBuilder {

  public BuilderFactoryBinary factory;
  private List<ProtocolEntity> protocols;
  private BinaryBuilderAdvanced binaryBuilderAdvanced;
  private ComparisonBuilderBinary comparisonBuilderBinary;
  private BinaryBuilder binaryBuilder;

  private ProtocolBuilderBinary(BuilderFactoryBinary factory) {
    this.factory = factory;
    this.protocols = new LinkedList<>();
  }

  public static SequentialBinaryBuilder createApplicationRoot(
      BuilderFactoryBinary factory,
      Consumer<SequentialBinaryBuilder> consumer) {
    SequentialBinaryBuilder builder = new SequentialBinaryBuilder(
        factory);
    builder
        .addConsumer(consumer, () -> new SequentialBinaryBuilder(factory));
    return builder;
  }
  
  public static SequentialBinaryBuilder createApplicationRoot(BuilderFactoryBinary factory) {
    return new SequentialBinaryBuilder(factory);
  }

  public BinaryBuilder binary() {
    if (this.binaryBuilder == null) {
      this.binaryBuilder = this.factory.createBinaryBuilder(this);
    }
    return this.binaryBuilder;
  }

  public BinaryBuilderAdvanced advancedBinary() {
    if (this.binaryBuilderAdvanced == null) {
      this.binaryBuilderAdvanced = this.factory.createAdvancedBinary(this);
    }
    return this.binaryBuilderAdvanced;
  }

  public ComparisonBuilderBinary comparison() {
    if (this.comparisonBuilderBinary == null) {
      this.comparisonBuilderBinary = this.factory.createComparison(this);
    }
    return this.comparisonBuilderBinary;
  }

  public BuilderFactoryBinary getFactory() {
    return this.factory;
  }

  <T extends ProtocolBuilderBinary> void addConsumer(Consumer<T> consumer, Supplier<T> supplier) {
    createAndAppend(new LazyProtocolProducerDecorator(() -> {
      T builder = supplier.get();
      consumer.accept(builder);
      return builder.build();
    }));
  }

  ProtocolEntity createAndAppend(ProtocolProducer producer) {
    ProtocolEntity protocolEntity = new ProtocolEntity(producer);
    protocols.add(protocolEntity);
    return protocolEntity;
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested parallel protocol
   * producer inserted into the original protocol producer.
   *
   * @param function of the protocol producer - will be lazy evaluated
   */
  public <R> Computation<R> createParallelSub(ComputationBuilderBinaryParallel<R> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.build(builder)),
        () -> new ParallelBinaryBuilder(factory));
    return result;
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested sequential protocol
   * producer inserted into the original protocol producer.
   *
   * @param function creation of the protocol producer - will be lazy evaluated
   */
  public <R> Computation<R> createSequentialSub(ComputationBuilderBinary<R> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.build(builder)),
        () -> new SequentialBinaryBuilder(factory));
    return result;
  }


  /**
   * Appends a concrete, native protocol to the list of producers - udeful for the native protocol
   * factroies that needs to be builders.
   *
   * @param nativeProtocol the native protocol to add
   * @param <T> the type of the native protocol - passthrough buildable object
   * @return the original native protocol.
   */
  public <T extends NativeProtocol> T append(T nativeProtocol) {
    createAndAppend(new SingleProtocolProducer(nativeProtocol));
    return nativeProtocol;
  }

  // This will go away and should not be used - users should recode their applications to
  // use closures
  @Override
  @Deprecated
  public <T extends ProtocolProducer> T append(T protocolProducer) {
    createAndAppend(protocolProducer);
    return protocolProducer;
  }

  void addEntities(ProtocolProducerCollection producerCollection) {
    for (ProtocolEntity protocolEntity : protocols) {
      producerCollection.append(protocolEntity.protocolProducer);
    }
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
  public static class SequentialBinaryBuilder extends ProtocolBuilderBinary {

    SequentialBinaryBuilder(BuilderFactoryBinary factory) {
      super(factory);
    }

    @Override
    public ProtocolProducer build() {
      SequentialProtocolProducer parallelProtocolProducer = new SequentialProtocolProducer();
      addEntities(parallelProtocolProducer);
      return parallelProtocolProducer;
    }

    public <R> BuildStepBinary<SequentialBinaryBuilder, R, Void> seq(
        ComputationBuilderBinary<R> function) {
      BuildStepBinary<SequentialBinaryBuilder, R, Void> builder =
          new BuildStepBinarySequential<>((ignored, inner) -> function.build(inner));
      createAndAppend(
          new LazyProtocolProducerDecorator(() -> builder.createProducer(null, getFactory())));
      return builder;
    }

    public <R> BuildStepBinary<ParallelBinaryBuilder, R, Void> par(
        ComputationBuilderBinaryParallel<R> f) {
      BuildStepBinary<ParallelBinaryBuilder, R, Void> builder =
          new BuildStepBinaryParallel<>((ignored, inner) -> f.build(inner));
      createAndAppend(
          new LazyProtocolProducerDecorator(() -> builder.createProducer(null, getFactory())));
      return builder;
    }

  }

  /**
   * A specific instance of the protocol builder that produces a parallel producer.
   */
  public static class ParallelBinaryBuilder extends ProtocolBuilderBinary {

    ParallelBinaryBuilder(BuilderFactoryBinary factory) {
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
