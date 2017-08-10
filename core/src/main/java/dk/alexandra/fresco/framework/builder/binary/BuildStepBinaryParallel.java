package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.ParallelBinaryBuilder;

/**
 * Created by pff on 30-06-2017.
 */
class BuildStepBinaryParallel<OutputT, InputT>
    extends BuildStepBinary<ParallelBinaryBuilder, OutputT, InputT> {

  BuildStepBinaryParallel(FrescoLambdaBinaryParallel<InputT, OutputT> function) {
    super(function);
  }

  @Override
  protected ParallelBinaryBuilder createBuilder(BuilderFactoryBinary factory) {
    return new ParallelBinaryBuilder(factory);
  }
}
