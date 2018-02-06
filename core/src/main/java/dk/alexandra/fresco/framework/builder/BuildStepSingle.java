package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.helper.LazyProtocolProducerDecorator;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;

class BuildStepSingle<BuilderT extends ProtocolBuilderImpl<BuilderT>, OutputT, InputT>
    implements BuildStep.NextStepBuilder<BuilderT, OutputT, InputT> {

  private boolean parallel;
  private FrescoLambda<InputT, BuilderT, OutputT> function;

  BuildStepSingle(FrescoLambda<InputT, BuilderT, OutputT> function, boolean parallel) {
    super();
    this.function = function;
    this.parallel = parallel;
  }

  public Pair<ProtocolProducer, DRes<OutputT>> createNextStep(
      InputT input,
      BuilderFactory<BuilderT> factory,
      BuildStep<OutputT, BuilderT, ?> next) {

    BuilderT builder = createBuilder(factory);
    DRes<OutputT> output = function.buildComputation(builder, input);
    if (next != null) {
      SequentialProtocolProducer protocolProducer = new SequentialProtocolProducer(
          builder.build(),
          new LazyProtocolProducerDecorator(() -> {
            OutputT out = null;
            if (output != null) {
              out = output.out();
            }
            return next.createProducer(out, factory);
          })
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
