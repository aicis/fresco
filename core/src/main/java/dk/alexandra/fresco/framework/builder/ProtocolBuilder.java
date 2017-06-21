package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.RightShiftBuilder;
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
 * the sequence in which they are created.
 */
public abstract class ProtocolBuilder<SIntT extends SInt> {

  private BasicNumericFactory<SIntT> basicNumericFactory;
  private List<ProtocolEntity> protocols;
  protected BuilderFactoryNumeric<SIntT> factory;

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

  public SIntT createConstant(int value) {
    return (SIntT) getSIntFactory().getSInt(value);
  }

  public static <SIntT extends SInt> SequentialProtocolBuilder<SIntT> createRoot(
      BuilderFactoryNumeric<SIntT> factory, Consumer<SequentialProtocolBuilder<SIntT>> consumer) {
    SequentialProtocolBuilder<SIntT> builder = new SequentialProtocolBuilder<>(factory);
    builder.addConsumer(consumer, () -> new SequentialProtocolBuilder<>(factory));
    return builder;
  }

  /**
   * Re-creates this basicNumericFactory based on a parallel protocol producer inserted into the
   * original protocol producer.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public <T extends Consumer<ParallelProtocolBuilder<SIntT>>>
  T createParallelSubFactory(T consumer) {
    addConsumer(consumer, () -> new ParallelProtocolBuilder<>(factory));
    return consumer;
  }

  public <R>
  Computation<R> createParallelSubFactoryReturning(
      Function<ParallelProtocolBuilder<SIntT>, Computation<R>> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.apply(builder)),
        () -> new ParallelProtocolBuilder<>(factory));
    return result;
  }

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

  public NumericBuilder<SIntT> createNumericBuilder() {
    return factory.createNumericBuilder(this);
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
    SomeBuilder<SequentialProtocolBuilder<SIntT>, SIntT, R, Void> seq(
        Function<SequentialProtocolBuilder<SIntT>, Computation<R>> function) {
      SomeBuilder<SequentialProtocolBuilder<SIntT>, SIntT, R, Void> builder =
          new SomeBuilderSequential<>((ignored, inner) -> function.apply(inner));
      ProtocolEntity protocolEntity = createAndAppend();
      protocolEntity.child = new LazyProtocolProducer(
          () -> builder.createProducer(null, factory)
      );
      return builder;
    }

    public <R>
    SomeBuilder<ParallelProtocolBuilder<SIntT>, SIntT, R, Void> par(
        Function<ParallelProtocolBuilder<SIntT>, Computation<R>> function) {
      SomeBuilder<ParallelProtocolBuilder<SIntT>, SIntT, R, Void> builder =
          new SomeBuilderParallel<>((ignored, inner) -> function.apply(inner));
      ProtocolEntity protocolEntity = createAndAppend();
      protocolEntity.child = new LazyProtocolProducer(
          () -> builder.createProducer(null, factory)
      );
      return builder;
    }
  }

  public static abstract class SomeBuilder<BuilderT extends ProtocolBuilder<SIntT>, SIntT extends SInt, OutputT, InputT> implements
      Computation<OutputT> {

    private final BiFunction<InputT, BuilderT, Computation<OutputT>> function;

    private SomeBuilder<?, SIntT, ?, OutputT> child;
    private Computation<OutputT> output;

    private SomeBuilder(
        BiFunction<InputT, BuilderT, Computation<OutputT>> function) {
      this.function = function;
    }

    public <NextOutputT> SomeBuilder<SequentialProtocolBuilder<SIntT>, SIntT, NextOutputT, OutputT> seq(
        BiFunction<OutputT, SequentialProtocolBuilder<SIntT>, Computation<NextOutputT>> function) {
      SomeBuilder<SequentialProtocolBuilder<SIntT>, SIntT, NextOutputT, OutputT> localChild =
          new SomeBuilderSequential<>(function);
      this.child = localChild;
      return localChild;
    }

    public <NextOutputT> SomeBuilder<ParallelProtocolBuilder<SIntT>, SIntT, NextOutputT, OutputT> par(
        BiFunction<OutputT, ParallelProtocolBuilder<SIntT>, Computation<NextOutputT>> function) {
      SomeBuilder<ParallelProtocolBuilder<SIntT>, SIntT, NextOutputT, OutputT> localChild =
          new SomeBuilderParallel<>(function);
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

  private static class SomeBuilderParallel<SIntT extends SInt, OutputT, InputT>
      extends SomeBuilder<ParallelProtocolBuilder<SIntT>, SIntT, OutputT, InputT> {

    private SomeBuilderParallel(
        BiFunction<InputT, ParallelProtocolBuilder<SIntT>, Computation<OutputT>> function) {
      super(function);
    }

    @Override
    protected ParallelProtocolBuilder<SIntT> createBuilder(BuilderFactoryNumeric<SIntT> factory) {
      return new ParallelProtocolBuilder<>(factory);
    }

  }


  private static class SomeBuilderSequential<SIntT extends SInt, OutputT, InputT>
      extends SomeBuilder<SequentialProtocolBuilder<SIntT>, SIntT, OutputT, InputT> {

    private SomeBuilderSequential(
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
