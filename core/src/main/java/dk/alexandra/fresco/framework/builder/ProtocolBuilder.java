package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.RightShiftBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.SIntFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Central class that builds complex trees of protocol producers based on
 * the sequence in which they are created.
 * <p>This class stores the intention of building
 * a protocol producer rather than the actual protocol producer and only when requested
 * actually evaluates the closure and returns the actual protocol producer.</p>
 * <p>This class also exposes builders with an intuitive and readable api but
 * automatic creates native protocols and adds these to this protocol builder as
 * intentions to be resolved later</p>
 */
public abstract class ProtocolBuilder {

  private BasicNumericFactory basicNumericFactory;
  private List<ProtocolEntity> protocols;
  protected BuilderFactoryNumeric factory;
  private NumericBuilder numericBuilder;

  private ProtocolBuilder(BuilderFactoryNumeric factory) {
    this.factory = factory;
    this.basicNumericFactory = factory.getBasicNumericFactory();
    this.protocols = new LinkedList<>();
  }

  public OIntFactory getOIntFactory() {
    return basicNumericFactory;
  }

  public SIntFactory getSIntFactory() {
    return basicNumericFactory;
  }

  public static SequentialProtocolBuilder createRoot(
      BuilderFactoryNumeric factory, Consumer<SequentialProtocolBuilder> consumer) {
    SequentialProtocolBuilder builder = new SequentialProtocolBuilder(factory);
    builder.addConsumer(consumer, () -> new SequentialProtocolBuilder(factory));
    return builder;
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested parallel protocol
   * producer inserted into the original protocol producer.
   *
   * @param function of the protocol producer - will be lazy evaluated
   */
  public <R> Computation<R> createParallelSubFactoryReturning(
      Function<ParallelProtocolBuilder, Computation<R>> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.apply(builder)),
        () -> new ParallelProtocolBuilder(factory));
    return result;
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested sequential protocol
   * producer inserted into the original protocol producer.
   *
   * @param function creation of the protocol producer - will be lazy evaluated
   */
  public <R> Computation<R> createSequentialSubFactoryReturning(
      Function<SequentialProtocolBuilder, Computation<R>> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.apply(builder)),
        () -> new SequentialProtocolBuilder(factory));
    return result;
  }

  /**
   * Re-creates this basicNumericFactory based on a sequential protocol producer inserted into the
   * original protocol producer.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public <T extends Consumer<SequentialProtocolBuilder>>
  void createSequentialSubFactory(T consumer) {
    addConsumer(consumer, () -> new SequentialProtocolBuilder(factory));
  }

  <T extends ProtocolBuilder> void addConsumer(Consumer<T> consumer,
      Supplier<T> supplier) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.child = new LazyProtocolProducer(() -> {
      T builder = supplier.get();
      consumer.accept(builder);
      return builder.build();
    });
  }

  protected ProtocolEntity createAndAppend() {
    ProtocolEntity protocolEntity = new ProtocolEntity();
    protocols.add(protocolEntity);
    return protocolEntity;
  }

  public <T extends NativeProtocol> T append(T computation) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.protocolProducer = SingleProtocolProducer.wrap(computation);
    return computation;
  }

  public <T extends ProtocolProducer> T append(T protocolProducer) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.protocolProducer = protocolProducer;
    return protocolProducer;
  }

  public abstract ProtocolProducer build();

  void addEntities(ProtocolProducerCollection producerCollection) {
    for (ProtocolEntity protocolEntity : protocols) {
      if (protocolEntity.computation != null) {
        producerCollection.append(protocolEntity.computation);
      } else if (protocolEntity.protocolProducer != null) {
        producerCollection.append(protocolEntity.protocolProducer);
      } else {
        producerCollection.append(protocolEntity.child);
      }
    }
  }

  public NumericBuilder numeric() {
    if (numericBuilder == null) {
      numericBuilder = factory.createNumericBuilder(this);
    }
    return numericBuilder;
  }

  public ComparisonBuilder<SInt> comparison() {
    return factory.createComparisonBuilder(this);
  }

  public InnerProductBuilder createInnerProductBuilder() {
    return factory.createInnerProductBuilder(this);
  }

  public RandomAdditiveMaskBuilder createAdditiveMaskBuilder() {
    return factory.createAdditiveMaskBuilder(this);
  }

  public OpenBuilder createOpenBuilder() {
    return factory.createOpenBuilder(this);
  }

  public RightShiftBuilder createRightShiftBuilder() {
    return factory.createRightShiftBuilder(this);
  }


  private static class ProtocolEntity {

    Computation<?> computation;
    ProtocolProducer protocolProducer;
    LazyProtocolProducer child;
  }

  private static class LazyProtocolProducer implements ProtocolProducer {

    private ProtocolProducer protocolProducer;
    private Supplier<ProtocolProducer> child;

    LazyProtocolProducer(Supplier<ProtocolProducer> supplier) {
      this.child = supplier;
    }

    @Override
    public void getNextProtocols(ProtocolCollection protocolCollection) {
      checkReady();
      protocolProducer.getNextProtocols(protocolCollection);
    }

    @Override
    public boolean hasNextProtocols() {
      checkReady();
      return protocolProducer.hasNextProtocols();
    }

    private void checkReady() {
      if (protocolProducer == null) {
        protocolProducer = child.get();
        child = null;
      }
    }
  }

  public static class SequentialProtocolBuilder extends ProtocolBuilder {

    private SequentialProtocolBuilder(BuilderFactoryNumeric factory) {
      super(factory);
    }

    @Override
    public ProtocolProducer build() {
      SequentialProtocolProducer parallelProtocolProducer = new SequentialProtocolProducer();
      addEntities(parallelProtocolProducer);
      return parallelProtocolProducer;
    }


    public <R>
    BuildStep<SequentialProtocolBuilder, R, Void> seq(
        Function<SequentialProtocolBuilder, Computation<R>> function) {
      BuildStep<SequentialProtocolBuilder, R, Void> builder =
          new BuildStepSequential<>((ignored, inner) -> function.apply(inner));
      ProtocolEntity protocolEntity = createAndAppend();
      protocolEntity.child = new LazyProtocolProducer(
          () -> builder.createProducer(null, factory)
      );
      return builder;
    }

    public <R>
    BuildStep<ParallelProtocolBuilder, R, Void> par(
        Function<ParallelProtocolBuilder, Computation<R>> function) {
      BuildStep<ParallelProtocolBuilder, R, Void> builder =
          new BuildStepParallel<>((ignored, inner) -> function.apply(inner));
      ProtocolEntity protocolEntity = createAndAppend();
      protocolEntity.child = new LazyProtocolProducer(
          () -> builder.createProducer(null, factory)
      );
      return builder;
    }
  }

  public static abstract class BuildStep<BuilderT extends ProtocolBuilder, OutputT, InputT> implements
      Computation<OutputT> {

    private final BiFunction<InputT, BuilderT, Computation<OutputT>> function;

    private BuildStep<?, ?, OutputT> child;
    private Computation<OutputT> output;

    private BuildStep(
        BiFunction<InputT, BuilderT, Computation<OutputT>> function) {
      this.function = function;
    }

    public <NextOutputT> BuildStep<SequentialProtocolBuilder, NextOutputT, OutputT> seq(
        BiFunction<OutputT, SequentialProtocolBuilder, Computation<NextOutputT>> function) {
      BuildStep<SequentialProtocolBuilder, NextOutputT, OutputT> localChild =
          new BuildStepSequential<>(function);
      this.child = localChild;
      return localChild;
    }

    public <NextOutputT> BuildStep<ParallelProtocolBuilder, NextOutputT, OutputT> par(
        BiFunction<OutputT, ParallelProtocolBuilder, Computation<NextOutputT>> function) {
      BuildStep<ParallelProtocolBuilder, NextOutputT, OutputT> localChild =
          new BuildStepParallel<>(function);
      this.child = localChild;
      return localChild;
    }

    public <FirstOutputT, SecondOutputT> BuildStep<ParallelProtocolBuilder, Pair<FirstOutputT, SecondOutputT>, OutputT> par(
        BiFunction<OutputT, SequentialProtocolBuilder, Computation<FirstOutputT>> firstFunction,
        BiFunction<OutputT, SequentialProtocolBuilder, Computation<SecondOutputT>> secondFunction) {
      BuildStep<ParallelProtocolBuilder, Pair<FirstOutputT, SecondOutputT>, OutputT> localChild =
          new BuildStepParallel<>(
              (OutputT output1, ParallelProtocolBuilder builder) -> {
                Computation<FirstOutputT> firstOutput =
                    builder.createSequentialSubFactoryReturning(
                        seq -> firstFunction.apply(output1, seq));
                Computation<SecondOutputT> secondOutput =
                    builder.createSequentialSubFactoryReturning(
                        seq -> secondFunction.apply(output1, seq));
                return () -> new Pair<>(firstOutput.out(), secondOutput.out());
              }
          );
      this.child = localChild;
      return localChild;
    }

    public OutputT out() {
      return output.out();
    }

    private ProtocolProducer createProducer(
        InputT input,
        BuilderFactoryNumeric factory) {

      BuilderT builder = createBuilder(factory);
      Computation<OutputT> output = function.apply(input, builder);
      if (child != null) {
        return
            new SequentialProtocolProducer(
                builder.build(),
                new LazyProtocolProducer(() ->
                    child.createProducer(output.out(), factory)
                )
            );
      } else {
        this.output = output;
        return builder.build();
      }
    }

    protected abstract BuilderT createBuilder(BuilderFactoryNumeric factory);
  }

  private static class BuildStepParallel<OutputT, InputT>
      extends BuildStep<ParallelProtocolBuilder, OutputT, InputT> {

    private BuildStepParallel(
        BiFunction<InputT, ParallelProtocolBuilder, Computation<OutputT>> function) {
      super(function);
    }

    @Override
    protected ParallelProtocolBuilder createBuilder(BuilderFactoryNumeric factory) {
      return new ParallelProtocolBuilder(factory);
    }

  }


  private static class BuildStepSequential<OutputT, InputT>
      extends BuildStep<SequentialProtocolBuilder, OutputT, InputT> {

    private BuildStepSequential(
        BiFunction<InputT, SequentialProtocolBuilder, Computation<OutputT>> function) {
      super(function);
    }

    @Override
    protected SequentialProtocolBuilder createBuilder(BuilderFactoryNumeric factory) {
      return new SequentialProtocolBuilder(factory);
    }

  }

  public static class ParallelProtocolBuilder extends ProtocolBuilder {

    private ParallelProtocolBuilder(BuilderFactoryNumeric factory) {
      super(factory);
    }

    @Override
    public ProtocolProducer build() {
      SequentialProtocolProducer parallelProtocolProducer = new SequentialProtocolProducer();
      addEntities(parallelProtocolProducer);
      return parallelProtocolProducer;
    }
  }
}
