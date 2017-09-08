package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.util.Pair;
import java.util.function.Predicate;

/**
 * A single step in the chained list of lambda that creates protocols to evaluate. The root step is
 * defined in the ProbtocolBuilderImpl.
 *
 * @param <InputT> the type of input for this binary step (the previous step's output).
 * @param <BuilderT> the type iof builder, currently either numeric or binary
 * @param <OutputT> the output of the build step
 */
public final class BuildStep<
    InputT, BuilderT extends ProtocolBuilderImpl<BuilderT>, OutputT>
    implements DRes<OutputT> {

  interface NextStepBuilder<
      BuilderT extends ProtocolBuilderImpl<BuilderT>,
      OutputT,
      InputT> {

    Pair<ProtocolProducer, DRes<OutputT>> createNextStep(
        InputT input,
        BuilderFactory<BuilderT> factory,
        BuildStep<OutputT, BuilderT, ?> next);
  }

  private NextStepBuilder<BuilderT, OutputT, InputT> stepBuilder;
  private BuildStep<OutputT, BuilderT, ?> next;
  private DRes<OutputT> output;

  BuildStep(NextStepBuilder<BuilderT, OutputT, InputT> stepBuilder) {
    this.stepBuilder = stepBuilder;
  }

  /**
   * Creates a new Build step based on this builder but with a subsequent producer inserted after
   * the original protocol producer.
   *
   * @param function creation of the protocol producer - will be lazy evaluated
   */
  public <NextOutputT> BuildStep<OutputT, BuilderT, NextOutputT> seq(
      FrescoLambda<OutputT, BuilderT, NextOutputT> function) {
    BuildStep<OutputT, BuilderT, NextOutputT> localChild =
        new BuildStep<>(new BuildStepSingle<>(function, false));
    this.next = localChild;
    return localChild;
  }


  /**
   * Creates a new Build step based on this builder but with a subsequent parallel producer inserted
   * after the original protocol producer.
   *
   * @param function of the protocol producer - will be lazy evaluated
   */
  public <NextOutputT> BuildStep<OutputT, BuilderT, NextOutputT> par(
      FrescoLambdaParallel<OutputT, BuilderT, NextOutputT> function) {
    BuildStep<OutputT, BuilderT, NextOutputT> localChild =
        new BuildStep<>(new BuildStepSingle<>(function, true));
    this.next = localChild;
    return localChild;
  }

  /**
   * Creates a looping Build step based on this builder but with a subsequent producer inserted
   * after the original protocol producer. This simulates the while functionality in java.
   *
   * @param test the predicate - as long as it evaluates to true, the function will be evaluated
   * @param function creation of the protocol producer for a single loop step - will be
   *     evaluated for each iteration
   */
  public BuildStep<OutputT, BuilderT, OutputT> whileLoop(
      Predicate<OutputT> test,
      FrescoLambda<OutputT, BuilderT, OutputT> function) {
    BuildStep<OutputT, BuilderT, OutputT> localChild =
        new BuildStep<>(new BuildStepLooping<>(test, function));
    this.next = localChild;
    return localChild;
  }


  /**
   * Creates a new Build step based on this builder but with a subsequent producer inserted after
   * the original protocol producer. The two producer will be evalueted in parallel, however each
   * of the two functions will be evaluated in sequence.
   *
   * @param firstFunction of the first protocol producer - will be lazy evaluated
   * @param secondFunction of the second protocol producer - will be lazy evaluated
   */
  public <FirstOutputT, SecondOutputT>
  BuildStep<OutputT, BuilderT, Pair<FirstOutputT, SecondOutputT>> pairInPar(
      FrescoLambda<OutputT, BuilderT, FirstOutputT> firstFunction,
      FrescoLambda<OutputT, BuilderT, SecondOutputT> secondFunction) {
    BuildStep<OutputT, BuilderT, Pair<FirstOutputT, SecondOutputT>>
        localChild = new BuildStep<>(
        new BuildStepSingle<>(
            (BuilderT builder, OutputT output1) -> {
              DRes<FirstOutputT> firstOutput =
                  builder.seq(
                      seq -> firstFunction.buildComputation(seq, output1));
              DRes<SecondOutputT> secondOutput =
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
    Pair<ProtocolProducer, DRes<OutputT>> nextStep =
        stepBuilder.createNextStep(input, factory, next);
    stepBuilder = null;
    output = nextStep.getSecond();
    return nextStep.getFirst();
  }
}
