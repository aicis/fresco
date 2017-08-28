package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import java.util.function.BiFunction;

class BuildStepSingle<BuilderT extends ProtocolBuilderImpl<BuilderT>, OutputT, InputT>
    implements BuildStep.NextStepBuilder<BuilderT, OutputT, InputT> {

  private boolean parallel;
  private BiFunction<InputT, BuilderT, Computation<OutputT>> function;

  BuildStepSingle(FrescoLambda<InputT, BuilderT, OutputT> function, boolean parallel) {
    super();
    this.function = function;
    this.parallel = parallel;
  }

  public Pair<ProtocolProducer, Computation<OutputT>> createNextStep(
      InputT input,
      BuilderFactory<BuilderT> factory,
      BuildStep<BuilderT, ?, OutputT> next) {

    BuilderT builder = createBuilder(factory);
    Computation<OutputT> output = function.apply(input, builder);
    if (next != null) {
      SequentialProtocolProducer protocolProducer = new SequentialProtocolProducer(
          builder.build(),
          new LazyProtocolProducerDecorator(() -> next.createProducer(output.out(), factory))
      );
      return new Pair<>(protocolProducer, null);
    } else {
      return new Pair<>(builder.build(), output);
    }
  }

  private BuilderT createBuilder(BuilderFactory<BuilderT> factory) {
    if (parallel) {
      return factory.createParallel();
    } else {
      return factory.createSequential();
    }
  }
}
