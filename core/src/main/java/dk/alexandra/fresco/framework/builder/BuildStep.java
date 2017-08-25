package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public abstract class BuildStep<
    BuilderT extends ProtocolBuilder<BuilderSequentialT>,
    BuilderSequentialT extends ProtocolBuilder<BuilderSequentialT>,
    BuilderParallelT extends ProtocolBuilder<BuilderSequentialT>,
    OutputT,
    InputT>
    implements Computation<OutputT> {

  protected final BiFunction<InputT, BuilderT, Computation<OutputT>> function;

  protected BuildStep<?, BuilderSequentialT, BuilderParallelT, ?, OutputT> next;
  protected Computation<OutputT> output;

  BuildStep(
      BiFunction<InputT, BuilderT, Computation<OutputT>> function) {
    this.function = function;
  }

  public <NextOutputT> BuildStep<BuilderSequentialT, BuilderSequentialT, BuilderParallelT, NextOutputT, OutputT> seq(
      FrescoLambda<OutputT, BuilderSequentialT, NextOutputT> function) {
    BuildStep<BuilderSequentialT, BuilderSequentialT, BuilderParallelT, NextOutputT, OutputT> localChild =
        new BuildStepSequential<>(function);
    this.next = localChild;
    return localChild;
  }

  public <NextOutputT> BuildStep<BuilderParallelT, BuilderSequentialT, BuilderParallelT, NextOutputT, OutputT> par(
      FrescoLambdaParallel<OutputT, BuilderParallelT, NextOutputT> function) {
    BuildStep<BuilderParallelT, BuilderSequentialT, BuilderParallelT, NextOutputT, OutputT> localChild =
        new BuildStepParallel<>(function);
    this.next = localChild;
    return localChild;
  }

  public BuildStep<BuilderSequentialT, BuilderSequentialT, BuilderParallelT, OutputT, OutputT> whileLoop(
      Predicate<OutputT> test,
      FrescoLambda<OutputT, BuilderSequentialT, OutputT> function) {
    BuildStepLooping<BuilderSequentialT, BuilderParallelT, OutputT> localChild = new BuildStepLooping<>(
        test, function);
    this.next = localChild;
    return localChild;
  }

  public <FirstOutputT, SecondOutputT>
  BuildStep<BuilderParallelT, BuilderSequentialT, BuilderParallelT, Pair<FirstOutputT, SecondOutputT>, OutputT> par(
      FrescoLambda<OutputT, BuilderSequentialT, FirstOutputT> firstFunction,
      FrescoLambda<OutputT, BuilderSequentialT, SecondOutputT> secondFunction) {
    BuildStep<
        BuilderParallelT,
        BuilderSequentialT,
        BuilderParallelT,
        Pair<FirstOutputT, SecondOutputT>,
        OutputT> localChild =
        new BuildStepParallel<>(
            (OutputT output1, BuilderParallelT builder) -> {
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

  protected ProtocolProducer createProducer(
      InputT input,
      BuilderFactory<BuilderSequentialT, BuilderParallelT> factory) {

    BuilderT builder = createBuilder(factory);
    Computation<OutputT> output = function.apply(input, builder);
    if (next != null) {
      return
          new SequentialProtocolProducer(
              builder.build(),
              new LazyProtocolProducerDecorator(() ->
                  next.createProducer(output.out(), factory)
              )
          );
    } else {
      this.output = output;
      return builder.build();
    }
  }

  static class BuildStepSequential<BuilderSequentialT extends ProtocolBuilder<BuilderSequentialT>,
      BuilderParallelT extends ProtocolBuilder<BuilderSequentialT>, OutputT, InputT>
      extends
      BuildStep<BuilderSequentialT,
          BuilderSequentialT,
          BuilderParallelT, OutputT, InputT> {

    BuildStepSequential(FrescoLambda<InputT, BuilderSequentialT, OutputT> function) {
      super(function);
    }

    @Override
    protected BuilderSequentialT createBuilder(
        BuilderFactory<BuilderSequentialT, BuilderParallelT> factory) {
      return factory.createSequential();
    }
  }

  protected abstract BuilderT createBuilder(
      BuilderFactory<BuilderSequentialT, BuilderParallelT> factory);

  /**
   * Created by pff on 30-06-2017.
   */
  static class BuildStepParallel<BuilderSequentialT extends ProtocolBuilder<BuilderSequentialT>,
      BuilderParallelT extends ProtocolBuilder<BuilderSequentialT>, OutputT, InputT>
      extends
      BuildStep<BuilderParallelT,
          BuilderSequentialT,
          BuilderParallelT, OutputT, InputT> {

    BuildStepParallel(FrescoLambdaParallel<InputT, BuilderParallelT, OutputT> function) {
      super(function);
    }

    @Override
    protected BuilderParallelT createBuilder(
        BuilderFactory<BuilderSequentialT, BuilderParallelT> factory) {
      return factory.createParallel();
    }
  }
}
