package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.ParallelNumericBuilder;

/**
 * Created by pff on 30-06-2017.
 */
class BuildStepParallel<OutputT, InputT>
    extends
    BuildStep<ParallelNumericBuilder, OutputT, InputT> {

  BuildStepParallel(FrescoLambdaParallel<InputT, OutputT> function) {
    super(function);
  }

  @Override
  protected ParallelNumericBuilder createBuilder(
      BuilderFactoryNumeric factory) {
    return new ParallelNumericBuilder(factory);
  }
}
