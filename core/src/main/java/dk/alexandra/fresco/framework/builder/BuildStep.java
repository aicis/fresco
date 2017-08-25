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
    BuilderT extends ProtocolBuilderImpl<BuilderT>,
    OutputT,
    InputT>
    implements Computation<OutputT> {

  protected final BiFunction<InputT, BuilderT, Computation<OutputT>> function;

  protected BuildStep<BuilderT, ?, OutputT> next;
  protected Computation<OutputT> output;

  BuildStep(
      BiFunction<InputT, BuilderT, Computation<OutputT>> function) {
    this.function = function;
  }

  public <NextOutputT> BuildStep<BuilderT, NextOutputT, OutputT> seq(
      FrescoLambda<OutputT, BuilderT, NextOutputT> function) {
    BuildStep<BuilderT, NextOutputT, OutputT> localChild =
        new BuildStepSequential<>(function);
    this.next = localChild;
    return localChild;
  }

  public <NextOutputT> BuildStep<BuilderT, NextOutputT, OutputT> par(
      FrescoLambda<OutputT, BuilderT, NextOutputT> function) {
    BuildStep<BuilderT, NextOutputT, OutputT> localChild =
        new BuildStepParallel<>(function);
    this.next = localChild;
    return localChild;
  }

  public BuildStep<BuilderT, OutputT, OutputT> whileLoop(
      Predicate<OutputT> test,
      FrescoLambda<OutputT, BuilderT, OutputT> function) {
    BuildStepLooping<BuilderT, OutputT> localChild = new BuildStepLooping<>(
        test, function);
    this.next = localChild;
    return localChild;
  }

  public <FirstOutputT, SecondOutputT>
  BuildStep<BuilderT, Pair<FirstOutputT, SecondOutputT>, OutputT> par(
      FrescoLambda<OutputT, BuilderT, FirstOutputT> firstFunction,
      FrescoLambda<OutputT, BuilderT, SecondOutputT> secondFunction) {
    BuildStep<BuilderT, Pair<FirstOutputT, SecondOutputT>, OutputT>
        localChild =
        new BuildStepParallel<>(
            (OutputT output1, BuilderT builder) -> {
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
      BuilderFactory<BuilderT> factory) {

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

  protected abstract BuilderT createBuilder(BuilderFactory<BuilderT> factory);

  static class BuildStepSequential<
      BuilderT extends ProtocolBuilderImpl<BuilderT>, OutputT, InputT>
      extends BuildStep<BuilderT, OutputT, InputT> {

    BuildStepSequential(FrescoLambda<InputT, BuilderT, OutputT> function) {
      super(function);
    }

    @Override
    protected BuilderT createBuilder(BuilderFactory<BuilderT> factory) {
      return factory.createSequential();
    }
  }

  static class BuildStepParallel<BuilderT extends ProtocolBuilderImpl<BuilderT>, OutputT, InputT>
      extends BuildStep<BuilderT, OutputT, InputT> {

    BuildStepParallel(FrescoLambda<InputT, BuilderT, OutputT> function) {
      super(function);
    }

    @Override
    protected BuilderT createBuilder(BuilderFactory<BuilderT> factory) {
      return factory.createParallel();
    }
  }
}
