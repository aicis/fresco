package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.util.Pair;
import java.util.function.Predicate;

public final class BuildStep<
    BuilderT extends ProtocolBuilderImpl<BuilderT>,
    OutputT,
    InputT>
    implements Computation<OutputT> {

  interface NextStepBuilder<
      BuilderT extends ProtocolBuilderImpl<BuilderT>,
      OutputT,
      InputT> {

    Pair<ProtocolProducer, Computation<OutputT>> createNextStep(
        InputT input,
        BuilderFactory<BuilderT> factory,
        BuildStep<BuilderT, ?, OutputT> next);
  }

  private NextStepBuilder<BuilderT, OutputT, InputT> stepBuilder;
  private BuildStep<BuilderT, ?, OutputT> next;
  private Computation<OutputT> output;

  BuildStep(NextStepBuilder<BuilderT, OutputT, InputT> stepBuilder) {
    this.stepBuilder = stepBuilder;
  }

  public <NextOutputT> BuildStep<BuilderT, NextOutputT, OutputT> seq(
      FrescoLambda<OutputT, BuilderT, NextOutputT> function) {
    BuildStep<BuilderT, NextOutputT, OutputT> localChild =
        new BuildStep<>(new BuildStepSingle<>(function, false));
    this.next = localChild;
    return localChild;
  }

  public <NextOutputT> BuildStep<BuilderT, NextOutputT, OutputT> par(
      FrescoLambda<OutputT, BuilderT, NextOutputT> function) {
    BuildStep<BuilderT, NextOutputT, OutputT> localChild =
        new BuildStep<>(new BuildStepSingle<>(function, true));
    this.next = localChild;
    return localChild;
  }

  public BuildStep<BuilderT, OutputT, OutputT> whileLoop(
      Predicate<OutputT> test,
      FrescoLambda<OutputT, BuilderT, OutputT> function) {
    BuildStep<BuilderT, OutputT, OutputT> localChild =
        new BuildStep<>(new BuildStepLooping<>(test, function));
    this.next = localChild;
    return localChild;
  }

  public <FirstOutputT, SecondOutputT>
  BuildStep<BuilderT, Pair<FirstOutputT, SecondOutputT>, OutputT> par(
      FrescoLambda<OutputT, BuilderT, FirstOutputT> firstFunction,
      FrescoLambda<OutputT, BuilderT, SecondOutputT> secondFunction) {
    BuildStep<BuilderT, Pair<FirstOutputT, SecondOutputT>, OutputT>
        localChild = new BuildStep<>(
        new BuildStepSingle<>(
            (BuilderT builder, OutputT output1) -> {
              Computation<FirstOutputT> firstOutput =
                  builder.seq(
                      seq -> firstFunction.buildComputation(seq, output1));
              Computation<SecondOutputT> secondOutput =
                  builder.seq(
                      seq -> secondFunction.buildComputation(seq, output1));
              return () -> new Pair<>(firstOutput.out(), secondOutput.out());
            }, true)
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
    Pair<ProtocolProducer, Computation<OutputT>> nextStep =
        stepBuilder.createNextStep(input, factory, next);
    stepBuilder = null;
    output = nextStep.getSecond();
    return nextStep.getFirst();
  }
}
