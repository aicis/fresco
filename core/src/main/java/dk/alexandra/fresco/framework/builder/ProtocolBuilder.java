package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BitLengthBuilder;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.RightShiftBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OIntFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
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

  public BasicNumericFactory getBasicNumericFactory() {
    return basicNumericFactory;
  }

  /**
   * Creates a root for this builder - should be applied when construcing
   * protocol producers from an {@link Application}.
   *
   * @param factory the protocol factory to get native protocols and composite builders from
   * @param consumer the root of the protocol producer
   * @return a sequential protocol builder that can create the protocol producer
   */
  public static SequentialProtocolBuilder createApplicationRoot(
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
  public <R> Computation<R> createParallelSub(ParallelComputationBuilder<R> function) {
    DelayedComputation<R> result = new DelayedComputation<>();
    addConsumer((builder) -> result.setComputation(function.build(builder)),
        () -> new ParallelProtocolBuilder(factory));
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
        () -> new SequentialProtocolBuilder(factory));
    return result;
  }

  /**
   * Creates another protocol builder based on th supplied consumer.
   * This method re-creates the builder based on a sequential protocol producer inserted into this
   * original protocol producer as a child.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public <T extends Consumer<SequentialProtocolBuilder>> void createIteration(T consumer) {
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

  ProtocolEntity createAndAppend() {
    ProtocolEntity protocolEntity = new ProtocolEntity();
    protocols.add(protocolEntity);
    return protocolEntity;
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
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.protocolProducer = SingleProtocolProducer.wrap(nativeProtocol);
    return nativeProtocol;
  }

  // This will go away and should not be used - users should recode their applications to
  // use closures
  @Deprecated
  public <T extends ProtocolProducer> T append(T protocolProducer) {
    ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.protocolProducer = protocolProducer;
    return protocolProducer;
  }

  /**
   * Building the actual protocol producer. Implementors decide which producer to create.
   *
   * @return the protocol producer that has been build
   */
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

  public AdvancedNumericBuilder createAdvancedNumericBuilder() {
    return factory.createAdvancedNumericBuilder(this);
  }

  public InputBuilder createInputBuilder() {
    return factory.createInputBuilder(this);
  }

  public BitLengthBuilder createBitLengthBuilder() {
    return factory.createBitLengthBuilder(this);
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

  /**
   * A specific instance of the protocol builder that produces a sequential producer.
   */
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


    public <R> BuildStep<SequentialProtocolBuilder, R, Void> seq(ComputationBuilder<R> function) {
      BuildStep<SequentialProtocolBuilder, R, Void> builder =
          new BuildStepSequential<>((ignored, inner) -> function.build(inner));
      ProtocolEntity protocolEntity = createAndAppend();
      protocolEntity.child = new LazyProtocolProducer(
          () -> builder.createProducer(null, factory)
      );
      return builder;
    }

    public <R> BuildStep<ParallelProtocolBuilder, R, Void> par(ParallelComputationBuilder<R> f) {
      BuildStep<ParallelProtocolBuilder, R, Void> builder =
          new BuildStepParallel<>((ignored, inner) -> f.build(inner));
      ProtocolEntity protocolEntity = createAndAppend();
      protocolEntity.child = new LazyProtocolProducer(
          () -> builder.createProducer(null, factory)
      );
      return builder;
    }
  }

  /**
   * A specific instance of the protocol builder that produces a parallel producer.
   */
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

  public static abstract class BuildStep<BuilderT extends ProtocolBuilder, OutputT, InputT>
      implements Computation<OutputT> {

    private final BiFunction<InputT, BuilderT, Computation<OutputT>> function;

    private BuildStep<?, ?, OutputT> child;
    private Computation<OutputT> output;

    private BuildStep(
        BiFunction<InputT, BuilderT, Computation<OutputT>> function) {
      this.function = function;
    }

    public <NextOutputT> BuildStep<SequentialProtocolBuilder, NextOutputT, OutputT> seq(
        FrescoLambda<OutputT, NextOutputT> function) {
      BuildStep<SequentialProtocolBuilder, NextOutputT, OutputT> localChild =
          new BuildStepSequential<>(function);
      this.child = localChild;
      return localChild;
    }

    public <NextOutputT> BuildStep<ParallelProtocolBuilder, NextOutputT, OutputT> par(
        FrescoLambdaParallel<OutputT, NextOutputT> function) {
      BuildStep<ParallelProtocolBuilder, NextOutputT, OutputT> localChild =
          new BuildStepParallel<>(function);
      this.child = localChild;
      return localChild;
    }

    public BuildStep<SequentialProtocolBuilder, OutputT, OutputT> whileLoop(
        Predicate<OutputT> test,
        BiFunction<OutputT, SequentialProtocolBuilder, Computation<OutputT>> function) {
      BuildStep<SequentialProtocolBuilder, OutputT, OutputT> localChild =
          new BuildStepSequential<>(
              (OutputT output1, SequentialProtocolBuilder builder) -> {
                DelayedComputation<OutputT> result = new DelayedComputation<>();
                whileStep(test, function, output1, builder, result);
                return result;
              });
      this.child = localChild;
      return localChild;
    }

    private void whileStep(Predicate<OutputT> test,
        BiFunction<OutputT, SequentialProtocolBuilder, Computation<OutputT>> function,
        OutputT lastOutput, SequentialProtocolBuilder builder,
        DelayedComputation<OutputT> result) {
      if (test.test(lastOutput)) {
        Computation<OutputT> nextOutput = function.apply(lastOutput, builder);
        builder.createIteration((nextBuilder) ->
            whileStep(test, function, nextOutput.out(), nextBuilder, result)
        );
      } else {
        result.setComputation(() -> lastOutput);
      }
    }

    public <FirstOutputT, SecondOutputT>
    BuildStep<ParallelProtocolBuilder, Pair<FirstOutputT, SecondOutputT>, OutputT> par(
        FrescoLambda<OutputT, FirstOutputT> firstFunction,
        FrescoLambda<OutputT, SecondOutputT> secondFunction) {
      BuildStep<ParallelProtocolBuilder, Pair<FirstOutputT, SecondOutputT>, OutputT> localChild =
          new BuildStepParallel<>(
              (OutputT output1, ParallelProtocolBuilder builder) -> {
                Computation<FirstOutputT> firstOutput =
                    builder.createSequentialSub(
                        seq -> firstFunction.apply(output1, seq));
                Computation<SecondOutputT> secondOutput =
                    builder.createSequentialSub(
                        seq -> secondFunction.apply(output1, seq));
                return () -> new Pair<>(firstOutput.out(), secondOutput.out());
              }
          );
      this.child = localChild;
      return localChild;
    }

    public OutputT out() {
      if (output != null) {
        return output.out();
      }
      return null;
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

    private BuildStepParallel(FrescoLambdaParallel<InputT, OutputT> function) {
      super(function);
    }

    @Override
    protected ParallelProtocolBuilder createBuilder(BuilderFactoryNumeric factory) {
      return new ParallelProtocolBuilder(factory);
    }
  }

  private static class BuildStepSequential<OutputT, InputT>
      extends BuildStep<SequentialProtocolBuilder, OutputT, InputT> {

    private BuildStepSequential(FrescoLambda<InputT, OutputT> function) {
      super(function);
    }

    @Override
    protected SequentialProtocolBuilder createBuilder(BuilderFactoryNumeric factory) {
      return new SequentialProtocolBuilder(factory);
    }

  }
}
