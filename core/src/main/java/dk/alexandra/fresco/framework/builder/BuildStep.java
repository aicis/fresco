package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.ParallelNumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public abstract class BuildStep<BuilderT extends ProtocolBuilder, OutputT, InputT>
    implements Computation<OutputT> {

  protected final BiFunction<InputT, BuilderT, Computation<OutputT>> function;

  protected BuildStep<?, ?, OutputT> next;
  protected Computation<OutputT> output;

  BuildStep(
      BiFunction<InputT, BuilderT, Computation<OutputT>> function) {
    this.function = function;
  }

  public <NextOutputT> BuildStep<SequentialNumericBuilder, NextOutputT, OutputT> seq(
      FrescoLambda<OutputT, NextOutputT> function) {
    BuildStep<SequentialNumericBuilder, NextOutputT, OutputT> localChild =
        new BuildStepSequential<>(function);
    this.next = localChild;
    return localChild;
  }

  public <NextOutputT> BuildStep<ParallelNumericBuilder, NextOutputT, OutputT> par(
      FrescoLambdaParallel<OutputT, NextOutputT> function) {
    BuildStep<ParallelNumericBuilder, NextOutputT, OutputT> localChild =
        new BuildStepParallel<>(function);
    this.next = localChild;
    return localChild;
  }

  public BuildStep<SequentialNumericBuilder, OutputT, OutputT> whileLoop(
      Predicate<OutputT> test,
      FrescoLambda<OutputT, OutputT> function) {
    BuildStepLooping<OutputT> localChild = new BuildStepLooping<>(
        test, function);
    this.next = localChild;
    return localChild;
  }

  public <FirstOutputT, SecondOutputT>
  BuildStep<ParallelNumericBuilder, Pair<FirstOutputT, SecondOutputT>, OutputT> par(
      FrescoLambda<OutputT, FirstOutputT> firstFunction,
      FrescoLambda<OutputT, SecondOutputT> secondFunction) {
    BuildStep<ParallelNumericBuilder, Pair<FirstOutputT, SecondOutputT>, OutputT> localChild =
        new BuildStepParallel<>(
            (OutputT output1, ParallelNumericBuilder builder) -> {
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
      BuilderFactoryNumeric factory) {

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

  protected abstract BuilderT createBuilder(BuilderFactoryNumeric factory);
}
