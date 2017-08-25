package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.util.Pair;
import java.util.function.Predicate;

public abstract class BuildStep<
    BuilderT extends ProtocolBuilderImpl<BuilderT>,
    OutputT,
    InputT>
    implements Computation<OutputT> {

  private BuildStep<BuilderT, ?, OutputT> next;
  private Computation<OutputT> output;

  public <NextOutputT> BuildStep<BuilderT, NextOutputT, OutputT> seq(
      FrescoLambda<OutputT, BuilderT, NextOutputT> function) {
    BuildStep<BuilderT, NextOutputT, OutputT> localChild =
        new BuildStepSingle<>(function, false);
    this.next = localChild;
    return localChild;
  }

  public <NextOutputT> BuildStep<BuilderT, NextOutputT, OutputT> par(
      FrescoLambda<OutputT, BuilderT, NextOutputT> function) {
    BuildStep<BuilderT, NextOutputT, OutputT> localChild =
        new BuildStepSingle<>(function, true);
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
        new BuildStepSingle<>(
            (OutputT output1, BuilderT builder) -> {
              Computation<FirstOutputT> firstOutput =
                  builder.createSequentialSub(
                      seq -> firstFunction.apply(output1, seq));
              Computation<SecondOutputT> secondOutput =
                  builder.createSequentialSub(
                      seq -> secondFunction.apply(output1, seq));
              return () -> new Pair<>(firstOutput.out(), secondOutput.out());
            }, true
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

  ProtocolProducer createProducer(
      InputT input,
      BuilderFactory<BuilderT> factory) {
    Pair<ProtocolProducer, Computation<OutputT>> nextStep = createNextStep(input, factory, next);
    output = nextStep.getSecond();
    return nextStep.getFirst();
  }

  protected abstract Pair<ProtocolProducer, Computation<OutputT>> createNextStep(
      InputT input,
      BuilderFactory<BuilderT> factory,
      BuildStep<BuilderT, ?, OutputT> next);

}
