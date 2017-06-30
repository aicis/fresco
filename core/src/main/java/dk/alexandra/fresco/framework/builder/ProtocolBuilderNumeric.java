package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
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
 * Central class for building protocols that are based on numeric protocols.
 */
public abstract class ProtocolBuilderNumeric implements ProtocolBuilder {

  private BasicNumericFactory basicNumericFactory;
  private List<ProtocolBuilderNumeric.ProtocolEntity> protocols;
  public BuilderFactoryNumeric factory;
  private NumericBuilder numericBuilder;

  private ProtocolBuilderNumeric(BuilderFactoryNumeric factory) {
    this.factory = factory;
    this.basicNumericFactory = factory.getBasicNumericFactory();
    this.protocols = new LinkedList<>();
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
  public static ProtocolBuilderNumeric.SequentialProtocolBuilder createApplicationRoot(
      BuilderFactoryNumeric factory,
      Consumer<ProtocolBuilderNumeric.SequentialProtocolBuilder> consumer) {
    ProtocolBuilderNumeric.SequentialProtocolBuilder builder = new ProtocolBuilderNumeric.SequentialProtocolBuilder(
        factory);
    builder
        .addConsumer(consumer, () -> new ProtocolBuilderNumeric.SequentialProtocolBuilder(factory));
    return builder;
  }

  public static SequentialProtocolBuilder createApplicationRoot(
      BuilderFactoryNumeric builderFactoryNumeric) {
    return new ProtocolBuilderNumeric.SequentialProtocolBuilder(builderFactoryNumeric);
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
        () -> new ProtocolBuilderNumeric.ParallelProtocolBuilder(factory));
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
        () -> new ProtocolBuilderNumeric.SequentialProtocolBuilder(factory));
    return result;
  }

  /**
   * Creates another protocol builder based on the supplied consumer.
   * This method re-creates the builder based on a sequential protocol producer inserted into this
   * original protocol producer as a child.
   *
   * @param consumer lazy creation of the protocol producer
   */
  public <T extends Consumer<ProtocolBuilderNumeric.SequentialProtocolBuilder>> void createIteration(
      T consumer) {
    addConsumer(consumer, () -> new ProtocolBuilderNumeric.SequentialProtocolBuilder(factory));
  }

  <T extends ProtocolBuilderNumeric> void addConsumer(Consumer<T> consumer,
      Supplier<T> supplier) {
    ProtocolBuilderNumeric.ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.child = new LazyProtocolProducer(() -> {
      T builder = supplier.get();
      consumer.accept(builder);
      return builder.build();
    });
  }

  ProtocolBuilderNumeric.ProtocolEntity createAndAppend() {
    ProtocolBuilderNumeric.ProtocolEntity protocolEntity = new ProtocolBuilderNumeric.ProtocolEntity();
    protocols.add(protocolEntity);
    return protocolEntity;
  }


  /**
   * Appends a concrete, native protocol to the list of producers - useful for the native protocol
   * factories that needs to be builders.
   *
   * @param nativeProtocol the native protocol to add
   * @param <T> the type of the native protocol - pass-through buildable object
   * @return the original native protocol.
   */
  public <T extends NativeProtocol> T append(T nativeProtocol) {
    ProtocolBuilderNumeric.ProtocolEntity protocolEntity = createAndAppend();
    protocolEntity.protocolProducer = SingleProtocolProducer.wrap(nativeProtocol);
    return nativeProtocol;
  }

  // This will go away and should not be used - users should recode their applications to
  // use closures
  @Deprecated
  public <T extends ProtocolProducer> T append(T protocolProducer) {
    ProtocolBuilderNumeric.ProtocolEntity protocolEntity = createAndAppend();
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
    for (ProtocolBuilderNumeric.ProtocolEntity protocolEntity : protocols) {
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

  public AdvancedNumericBuilder createAdvancedNumericBuilder() {
    return factory.createAdvancedNumericBuilder(this);
  }

  public MiscOIntGenerators getBigIntegerHelper() {
    return factory.getBigIntegerHelper();
  }

  private static class ProtocolEntity {

    Computation<?> computation;
    ProtocolProducer protocolProducer;
    LazyProtocolProducer child;
  }

  /**
   * A specific instance of the protocol builder that produces a sequential producer.
   */
  public static class SequentialProtocolBuilder extends ProtocolBuilderNumeric {

    private SequentialProtocolBuilder(BuilderFactoryNumeric factory) {
      super(factory);
    }

    @Override
    public ProtocolProducer build() {
      SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
      addEntities(sequentialProtocolProducer);
      return sequentialProtocolProducer;
    }


    public <R> BuildStep<SequentialProtocolBuilder, R, Void> seq(ComputationBuilder<R> function) {
      BuildStep<SequentialProtocolBuilder, R, Void> builder =
          new ProtocolBuilderNumeric.BuildStepSequential<>(
              (ignored, inner) -> function.build(inner));
      ProtocolBuilderNumeric.ProtocolEntity protocolEntity = createAndAppend();
      protocolEntity.child = new LazyProtocolProducer(
          () -> builder.createProducer(null, factory)
      );
      return builder;
    }

    public <R> BuildStep<ParallelProtocolBuilder, R, Void> par(ParallelComputationBuilder<R> f) {
      BuildStep<ParallelProtocolBuilder, R, Void> builder =
          new ProtocolBuilderNumeric.BuildStepParallel<>((ignored, inner) -> f.build(inner));
      ProtocolBuilderNumeric.ProtocolEntity protocolEntity = createAndAppend();
      protocolEntity.child = new LazyProtocolProducer(
          () -> builder.createProducer(null, factory)
      );
      return builder;
    }
  }

  /**
   * A specific instance of the protocol builder that produces a parallel producer.
   */
  public static class ParallelProtocolBuilder extends ProtocolBuilderNumeric {

    private ParallelProtocolBuilder(BuilderFactoryNumeric factory) {
      super(factory);
    }

    @Override
    public ProtocolProducer build() {
      ParallelProtocolProducer parallelProtocolProducer = new ParallelProtocolProducer();
      addEntities(parallelProtocolProducer);
      return parallelProtocolProducer;
    }
  }

  public static abstract class BuildStep<BuilderT extends ProtocolBuilderNumeric, OutputT, InputT>
      implements Computation<OutputT> {

    protected final BiFunction<InputT, BuilderT, Computation<OutputT>> function;

    protected ProtocolBuilderNumeric.BuildStep<?, ?, OutputT> next;
    protected Computation<OutputT> output;

    private BuildStep(
        BiFunction<InputT, BuilderT, Computation<OutputT>> function) {
      this.function = function;
    }

    public <NextOutputT> ProtocolBuilderNumeric.BuildStep<ProtocolBuilderNumeric.SequentialProtocolBuilder, NextOutputT, OutputT> seq(
        FrescoLambda<OutputT, NextOutputT> function) {
      ProtocolBuilderNumeric.BuildStep<ProtocolBuilderNumeric.SequentialProtocolBuilder, NextOutputT, OutputT> localChild =
          new ProtocolBuilderNumeric.BuildStepSequential<>(function);
      this.next = localChild;
      return localChild;
    }

    public <NextOutputT> ProtocolBuilderNumeric.BuildStep<ProtocolBuilderNumeric.ParallelProtocolBuilder, NextOutputT, OutputT> par(
        FrescoLambdaParallel<OutputT, NextOutputT> function) {
      ProtocolBuilderNumeric.BuildStep<ProtocolBuilderNumeric.ParallelProtocolBuilder, NextOutputT, OutputT> localChild =
          new ProtocolBuilderNumeric.BuildStepParallel<>(function);
      this.next = localChild;
      return localChild;
    }

    public ProtocolBuilderNumeric.BuildStep<ProtocolBuilderNumeric.SequentialProtocolBuilder, OutputT, OutputT> whileLoop(
        Predicate<OutputT> test,
        FrescoLambda<OutputT, OutputT> function) {
      ProtocolBuilderNumeric.BuildStepLooping<OutputT> localChild = new ProtocolBuilderNumeric.BuildStepLooping<>(
          test, function);
      this.next = localChild;
      return localChild;
    }

    public <FirstOutputT, SecondOutputT>
    ProtocolBuilderNumeric.BuildStep<ProtocolBuilderNumeric.ParallelProtocolBuilder, Pair<FirstOutputT, SecondOutputT>, OutputT> par(
        FrescoLambda<OutputT, FirstOutputT> firstFunction,
        FrescoLambda<OutputT, SecondOutputT> secondFunction) {
      ProtocolBuilderNumeric.BuildStep<ProtocolBuilderNumeric.ParallelProtocolBuilder, Pair<FirstOutputT, SecondOutputT>, OutputT> localChild =
          new ProtocolBuilderNumeric.BuildStepParallel<>(
              (OutputT output1, ProtocolBuilderNumeric.ParallelProtocolBuilder builder) -> {
                Computation<FirstOutputT> firstOutput =
                    builder.createSequentialSub(
                        seq -> firstFunction.apply(output1, seq));
                Computation<SecondOutputT> secondOutput =
                    builder.createSequentialSub(
                        seq -> secondFunction.apply(output1, seq));
                return () -> new Pair<>(firstOutput.out(), secondOutput.out());
              }
          );
      this.next = localChild;
      return localChild;
    }

    public OutputT out() {
      if (output != null) {
        return output.out();
      }
      return null;
    }

    protected ProtocolProducer createProducerInternal(InputT input, BuilderFactoryNumeric factory) {
      BuilderT builder = createBuilder(factory);
      this.output = function.apply(input, builder);
      return builder.build();
    }

    ProtocolProducer createProducer(InputT input, BuilderFactoryNumeric factory) {
      return new ChainedProtocolProducer(this, factory, () -> input);
    }

    protected abstract BuilderT createBuilder(BuilderFactoryNumeric factory);

  }

  private static class BuildStepParallel<OutputT, InputT>
      extends
      ProtocolBuilderNumeric.BuildStep<ProtocolBuilderNumeric.ParallelProtocolBuilder, OutputT, InputT> {

    private BuildStepParallel(FrescoLambdaParallel<InputT, OutputT> function) {
      super(function);
    }

    @Override
    protected ProtocolBuilderNumeric.ParallelProtocolBuilder createBuilder(
        BuilderFactoryNumeric factory) {
      return new ProtocolBuilderNumeric.ParallelProtocolBuilder(factory);
    }
  }


  private static class BuildStepLooping<InputT>
      extends
      ProtocolBuilderNumeric.BuildStep<ProtocolBuilderNumeric.SequentialProtocolBuilder, InputT, InputT> {

    private final Predicate<InputT> predicate;

    private BuildStepLooping(
        Predicate<InputT> predicate,
        FrescoLambda<InputT, InputT> function) {
      super(function);
      this.predicate = predicate;
    }

    @Override
    protected ProtocolProducer createProducerInternal(InputT input, BuilderFactoryNumeric factory) {
      final DelayedComputation<InputT> delayedComputation = new DelayedComputation<>();
      this.output = delayedComputation;
      return new ProtocolProducer() {
        private boolean isDone = false;
        private boolean doneWithOwn = false;
        Computation<InputT> currentResult;
        ProtocolProducer currentProducer = null;

        {
          updateToNextProducer(input);
          currentResult = () -> input;
        }

        @Override
        public void getNextProtocols(ProtocolCollection protocolCollection) {
          currentProducer.getNextProtocols(protocolCollection);
        }

        private void next() {
          while (!isDone && !currentProducer.hasNextProtocols()) {
            updateToNextProducer(currentResult.out());
          }
        }

        private void updateToNextProducer(InputT input) {
          if (doneWithOwn) {
            isDone = true;
          } else {
            if (predicate.test(input)) {
              ProtocolBuilderNumeric.SequentialProtocolBuilder builder = new ProtocolBuilderNumeric.SequentialProtocolBuilder(
                  factory);
              currentResult = function.apply(input, builder);
              currentProducer = builder.build();
            } else {
              doneWithOwn = true;
              delayedComputation.setComputation(currentResult);
              if (next != null) {
                currentProducer = next.createProducer(input, factory);
                next = null;
              }
            }
          }
        }

        @Override
        public boolean hasNextProtocols() {
          next();
          return !isDone;
        }
      };
    }

    @Override
    protected ProtocolBuilderNumeric.SequentialProtocolBuilder createBuilder(
        BuilderFactoryNumeric factory) {
      throw new IllegalStateException("Should not be called");
    }
  }

  private static class BuildStepSequential<OutputT, InputT>
      extends
      ProtocolBuilderNumeric.BuildStep<ProtocolBuilderNumeric.SequentialProtocolBuilder, OutputT, InputT> {

    private BuildStepSequential(FrescoLambda<InputT, OutputT> function) {
      super(function);
    }

    @Override
    protected ProtocolBuilderNumeric.SequentialProtocolBuilder createBuilder(
        BuilderFactoryNumeric factory) {
      return new ProtocolBuilderNumeric.SequentialProtocolBuilder(factory);
    }

  }

  private static class ChainedProtocolProducer implements ProtocolProducer {

    private final BuilderFactoryNumeric factory;
    private Computation lastOutput;
    private BuildStep nextStep;
    private ProtocolProducer currentProducer;

    ChainedProtocolProducer(BuildStep buildStep,
        BuilderFactoryNumeric factory, Computation inputLambda) {
      this.factory = factory;
      updateProducer(inputLambda, buildStep);
    }

    @Override
    public void getNextProtocols(ProtocolCollection protocolCollection) {
      currentProducer.getNextProtocols(protocolCollection);
    }

    @Override
    public boolean hasNextProtocols() {
      while (!currentProducer.hasNextProtocols() && nextStep != null) {
        updateProducer(lastOutput, nextStep);
      }
      return currentProducer.hasNextProtocols();
    }

    @SuppressWarnings("unchecked")
    private void updateProducer(Computation lastOutput, BuildStep step) {
      currentProducer = step.createProducerInternal(lastOutput.out(), factory);
      this.lastOutput = step.output;
      this.nextStep = step.next;
    }
  }
}
