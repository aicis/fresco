package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.ParallelBinaryBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public abstract class BuildStepBinary<BuilderT extends ProtocolBuilder, OutputT, InputT>
    implements Computation<OutputT> {

  protected final BiFunction<InputT, BuilderT, Computation<OutputT>> function;

  protected BuildStepBinary<?, ?, OutputT> next;
  protected Computation<OutputT> output;

  BuildStepBinary(BiFunction<InputT, BuilderT, Computation<OutputT>> function) {
    this.function = function;
  }

  public <NextOutputT> BuildStepBinary<SequentialBinaryBuilder, NextOutputT, OutputT> seq(
      FrescoLambdaBinary<OutputT, NextOutputT> function) {
    BuildStepBinary<SequentialBinaryBuilder, NextOutputT, OutputT> localChild =
        new BuildStepBinarySequential<>(function);
    this.next = localChild;
    return localChild;
  }

  public <NextOutputT> BuildStepBinary<ParallelBinaryBuilder, NextOutputT, OutputT> par(
      FrescoLambdaBinaryParallel<OutputT, NextOutputT> function) {
    BuildStepBinary<ParallelBinaryBuilder, NextOutputT, OutputT> localChild =
        new BuildStepBinaryParallel<>(function);
    this.next = localChild;
    return localChild;
  }

  public BuildStepBinary<SequentialBinaryBuilder, OutputT, OutputT> whileLoop(
      Predicate<OutputT> test, FrescoLambdaBinary<OutputT, OutputT> function) {
    BuildStepBinaryLooping<OutputT> localChild = new BuildStepBinaryLooping<>(test, function);
    this.next = localChild;
    return localChild;
  }

  public <FirstOutputT, SecondOutputT> BuildStepBinary<ParallelBinaryBuilder, Pair<FirstOutputT, SecondOutputT>, OutputT> par(
      FrescoLambdaBinary<OutputT, FirstOutputT> firstFunction,
      FrescoLambdaBinary<OutputT, SecondOutputT> secondFunction) {
    BuildStepBinary<ParallelBinaryBuilder, Pair<FirstOutputT, SecondOutputT>, OutputT> localChild =
        new BuildStepBinaryParallel<>((OutputT output1, ParallelBinaryBuilder builder) -> {
          Computation<FirstOutputT> firstOutput =
              builder.createSequentialSub(seq -> firstFunction.apply(output1, seq));
          Computation<SecondOutputT> secondOutput =
              builder.createSequentialSub(seq -> secondFunction.apply(output1, seq));
          return () -> new Pair<>(firstOutput.out(), secondOutput.out());
        });
    this.next = localChild;
    return localChild;
  }

  public OutputT out() {
    if (output != null) {
      return output.out();
    }
    return null;
  }

  protected ProtocolProducer createProducer(InputT input, BuilderFactoryBinary factory) {

    BuilderT builder = createBuilder(factory);
    Computation<OutputT> output = function.apply(input, builder);
    if (next != null) {
      return new SequentialProtocolProducer(builder.build(),
          new LazyProtocolProducerDecorator(() -> next.createProducer(output.out(), factory)));
    } else {
      this.output = output;
      return builder.build();
    }
  }

  protected abstract BuilderT createBuilder(BuilderFactoryBinary factory);
}
