package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.ParallelBinaryBuilder;
import java.util.function.BiFunction;

public interface FrescoLambdaBinaryParallel<InputT, OutputT>
    extends BiFunction<InputT, ParallelBinaryBuilder, Computation<OutputT>> {

}
