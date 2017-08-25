package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.BuildStep.BuildStepSequential;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilder;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilderAdvanced;
import dk.alexandra.fresco.framework.builder.binary.BinaryUtilityBuilder;
import dk.alexandra.fresco.framework.builder.binary.BristolCryptoBuilder;
import dk.alexandra.fresco.framework.builder.binary.ComparisonBuilderBinary;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ProtocolBuilderBinary implements ProtocolBuilder<SequentialBinaryBuilder> {

  public BuilderFactoryBinary factory;
  private List<ProtocolEntity> protocols;
  private BinaryBuilderAdvanced binaryBuilderAdvanced;
  private ComparisonBuilderBinary comparisonBuilderBinary;
  private BristolCryptoBuilder bristolCryptoBuilder;
  private BinaryBuilder binaryBuilder;
  private BinaryUtilityBuilder utilityBuilder;

  private ProtocolBuilderBinary(BuilderFactoryBinary factory) {
    this.factory = factory;
    this.protocols = new LinkedList<>();
  }

  public static SequentialBinaryBuilder createApplicationRoot(BuilderFactoryBinary factory,
      Consumer<SequentialBinaryBuilder> consumer) {
    SequentialBinaryBuilder builder = new SequentialBinaryBuilder(factory);
    builder.addConsumer(consumer, () -> new SequentialBinaryBuilder(factory));
    return builder;
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

  public BristolCryptoBuilder bristol() {
    if (this.bristolCryptoBuilder == null) {
      this.bristolCryptoBuilder = this.factory.createBristolCryptoBuilder(this);
    }
    return this.bristolCryptoBuilder;
  }

  public BinaryUtilityBuilder utility() {
    if (this.utilityBuilder == null) {
      this.utilityBuilder = this.factory.createUtilityBuilder(this);
    }
    return this.utilityBuilder;
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
  public <R> Computation<R> createParallelSub(
      ComputationBuilderParallel<R, ParallelBinaryBuilder> function) {
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
  @Override
  public <R> Computation<R> createSequentialSub(
      ComputationBuilder<R, SequentialBinaryBuilder> function) {
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

    public <R>
    BuildStep<SequentialBinaryBuilder, SequentialBinaryBuilder, ParallelBinaryBuilder, R, Void>
    seq(ComputationBuilder<R, SequentialBinaryBuilder> function) {
      BuildStep<SequentialBinaryBuilder, SequentialBinaryBuilder, ParallelBinaryBuilder, R, Void> builder =
          new BuildStepSequential<>((ignored, inner) -> function.build(inner));
      createAndAppend(
          new LazyProtocolProducerDecorator(() -> builder.createProducer(null, getFactory())));
      return builder;
    }

    public <R> BuildStep<ParallelBinaryBuilder, SequentialBinaryBuilder, ParallelBinaryBuilder, R, Void> par(
        ComputationBuilderParallel<R, ParallelBinaryBuilder> f) {
      BuildStep<ParallelBinaryBuilder, SequentialBinaryBuilder, ParallelBinaryBuilder, R, Void> builder =
          new BuildStep.BuildStepParallel<>((ignored, inner) -> f.build(inner));
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
