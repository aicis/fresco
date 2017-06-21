package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactoryNumeric;
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
 * Central class that allowes building complex trees of protocol producers based on
 * the sequence in which they are created. This class stores the intention of building
 * a protocol producer rather than the actual protocol producer and only when requested
 * actually evaluates the closure and returns the actual protocol producer.
 */
public abstract class ProtocolBuilder<SIntT extends SInt> {

  private BasicNumericFactory<SIntT> basicNumericFactory;
  private List<ProtocolEntity> protocols;
  protected BuilderFactoryNumeric<SIntT> factory;
  private NumericBuilder<SIntT> numericBuilder;

  private ProtocolBuilder(BuilderFactoryNumeric<SIntT> factory) {
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

  public static <SIntT extends SInt> SequentialProtocolBuilder<SIntT> createRoot(
      BuilderFactoryNumeric<SIntT> factory, Consumer<SequentialProtocolBuilder<SIntT>> consumer) {
    SequentialProtocolBuilder<SIntT> builder = new SequentialProtocolBuilder<>(factory);
    builder.addConsumer(consumer, () -> new SequentialProtocolBuilder<>(factory));
    return builder;
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested parallel protocol
   * producer inserted into the original protocol producer.
   *
   * @param function of the protocol producer - will be lazy evaluated
   */
  public <R>
  Computation<R> createParallelSubFactoryReturning(
      Function<ParallelProtocolBuilder<SIntT>, Computation<R>> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.apply(builder)),
        () -> new ParallelProtocolBuilder<>(factory));
    return result;
  }

  /**
   * Re-creates this builder based on this basicNumericFactory but with a nested sequential protocol
   * producer inserted into the original protocol producer.
   *
   * @param function creation of the protocol producer - will be lazy evaluated
   */
  public <R>
  Computation<R> createSequentialSubFactoryReturning(
      Function<SequentialProtocolBuilder<SIntT>, Computation<R>> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.apply(builder)),
        () -> new SequentialProtocolBuilder<>(factory));
    return result;
  }

  /**
   * Re-creates this basicNumericFactory based on a sequential protocol producer inserted into the
   * original protocol producer.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public <T extends Consumer<SequentialProtocolBuilder<SIntT>>>
  T createSequentialSubFactory(T consumer) {
    addConsumer(consumer, () -> new SequentialProtocolBuilder<>(factory));
    return consumer;
  }

  <T extends ProtocolBuilder<SIntT>> void addConsumer(Consumer<T> consumer,
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

  public NumericBuilder<SIntT> numeric() {
    if (numericBuilder == null) {
      numericBuilder = factory.createNumericBuilder(this);
    }
    return numericBuilder;
  }

  public ComparisonBuilder<SIntT> createComparisonBuilder() {
    return factory.createComparisonBuilder(this);
  }

  public InnerProductBuilder<SIntT> createInnerProductBuilder() {
    return factory.createInnerProductBuilder(this);
  }

  public RandomAdditiveMaskBuilder<SIntT> createAdditiveMaskBuilder() {
    return factory.createAdditiveMaskBuilder(this);
  }

  public OpenBuilder<SIntT> createOpenBuilder() {
    return factory.createOpenBuilder(this);
  }

  public RightShiftBuilder<SIntT> createRightShiftBuilder() {
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

  public static class SequentialProtocolBuilder<SIntT extends SInt> extends ProtocolBuilder<SIntT> {

    private SequentialProtocolBuilder(BuilderFactoryNumeric<SIntT> factory) {
      super(factory);
    }

    @Override
    public ProtocolProducer build() {
      SequentialProtocolProducer parallelProtocolProducer = new SequentialProtocolProducer();
      addEntities(parallelProtocolProducer);
      return parallelProtocolProducer;
    }


    public <R>
    BuildStep<SequentialProtocolBuilder<SIntT>, SIntT, R, Void> seq(
        Function<SequentialProtocolBuilder<SIntT>, Computation<R>> function) {
      BuildStep<SequentialProtocolBuilder<SIntT>, SIntT, R, Void> builder =
          new BuildStepSequential<>((ignored, inner) -> function.apply(inner));
      ProtocolEntity protocolEntity = createAndAppend();
      protocolEntity.child = new LazyProtocolProducer(
          () -> builder.createProducer(null, factory)
      );
      return builder;
    }

    public <R>
    BuildStep<ParallelProtocolBuilder<SIntT>, SIntT, R, Void> par(
        Function<ParallelProtocolBuilder<SIntT>, Computation<R>> function) {
      BuildStep<ParallelProtocolBuilder<SIntT>, SIntT, R, Void> builder =
          new BuildStepParallel<>((ignored, inner) -> function.apply(inner));
      ProtocolEntity protocolEntity = createAndAppend();
      protocolEntity.child = new LazyProtocolProducer(
          () -> builder.createProducer(null, factory)
      );
      return builder;
    }
  }

  public static abstract class BuildStep<BuilderT extends ProtocolBuilder<SIntT>, SIntT extends SInt, OutputT, InputT> implements
      Computation<OutputT> {

    private final BiFunction<InputT, BuilderT, Computation<OutputT>> function;

    private BuildStep<?, SIntT, ?, OutputT> child;
    private Computation<OutputT> output;

    private BuildStep(
        BiFunction<InputT, BuilderT, Computation<OutputT>> function) {
      this.function = function;
    }

    public <NextOutputT> BuildStep<SequentialProtocolBuilder<SIntT>, SIntT, NextOutputT, OutputT> seq(
        BiFunction<OutputT, SequentialProtocolBuilder<SIntT>, Computation<NextOutputT>> function) {
      BuildStep<SequentialProtocolBuilder<SIntT>, SIntT, NextOutputT, OutputT> localChild =
          new BuildStepSequential<>(function);
      this.child = localChild;
      return localChild;
    }

    public <NextOutputT> BuildStep<ParallelProtocolBuilder<SIntT>, SIntT, NextOutputT, OutputT> par(
        BiFunction<OutputT, ParallelProtocolBuilder<SIntT>, Computation<NextOutputT>> function) {
      BuildStep<ParallelProtocolBuilder<SIntT>, SIntT, NextOutputT, OutputT> localChild =
          new BuildStepParallel<>(function);
      this.child = localChild;
      return localChild;
    }

    public <FirstOutputT, SecondOutputT> BuildStep<ParallelProtocolBuilder<SIntT>, SIntT, Pair<FirstOutputT, SecondOutputT>, OutputT> par(
        BiFunction<OutputT, SequentialProtocolBuilder<SIntT>, Computation<FirstOutputT>> firstFunction,
        BiFunction<OutputT, SequentialProtocolBuilder<SIntT>, Computation<SecondOutputT>> secondFunction) {
      BuildStep<ParallelProtocolBuilder<SIntT>, SIntT, Pair<FirstOutputT, SecondOutputT>, OutputT> localChild =
          new BuildStepParallel<>(
              (OutputT output1, ParallelProtocolBuilder<SIntT> builder) -> {
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
        BuilderFactoryNumeric<SIntT> factory) {

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

    protected abstract BuilderT createBuilder(BuilderFactoryNumeric<SIntT> factory);
  }

  private static class BuildStepParallel<SIntT extends SInt, OutputT, InputT>
      extends BuildStep<ParallelProtocolBuilder<SIntT>, SIntT, OutputT, InputT> {

    private BuildStepParallel(
        BiFunction<InputT, ParallelProtocolBuilder<SIntT>, Computation<OutputT>> function) {
      super(function);
    }

    @Override
    protected ParallelProtocolBuilder<SIntT> createBuilder(BuilderFactoryNumeric<SIntT> factory) {
      return new ParallelProtocolBuilder<>(factory);
    }

  }


  private static class BuildStepSequential<SIntT extends SInt, OutputT, InputT>
      extends BuildStep<SequentialProtocolBuilder<SIntT>, SIntT, OutputT, InputT> {

    private BuildStepSequential(
        BiFunction<InputT, SequentialProtocolBuilder<SIntT>, Computation<OutputT>> function) {
      super(function);
    }

    @Override
    protected SequentialProtocolBuilder<SIntT> createBuilder(BuilderFactoryNumeric<SIntT> factory) {
      return new SequentialProtocolBuilder<>(factory);
    }

  }

  public static class ParallelProtocolBuilder<SIntT extends SInt> extends ProtocolBuilder<SIntT> {

    private ParallelProtocolBuilder(BuilderFactoryNumeric<SIntT> factory) {
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
