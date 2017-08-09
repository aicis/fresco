package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;

class BuildStepBinarySequential<OutputT, InputT>
    extends BuildStepBinary<SequentialBinaryBuilder, OutputT, InputT> {

  BuildStepBinarySequential(FrescoLambdaBinary<InputT, OutputT> function) {
    super(function);
  }

  @Override
  protected SequentialBinaryBuilder createBuilder(BuilderFactoryBinary factory) {
    return new SequentialBinaryBuilder(factory);
  }

}
