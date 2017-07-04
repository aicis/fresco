package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;

class BuildStepSequential<OutputT, InputT>
    extends BuildStep<SequentialNumericBuilder, OutputT, InputT> {

  BuildStepSequential(FrescoLambda<InputT, OutputT> function) {
    super(function);
  }

  @Override
  protected SequentialNumericBuilder createBuilder(
      BuilderFactoryNumeric factory) {
    return new SequentialNumericBuilder(factory);
  }

}
