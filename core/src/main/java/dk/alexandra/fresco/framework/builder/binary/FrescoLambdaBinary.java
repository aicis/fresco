package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import java.util.function.BiFunction;

public interface FrescoLambdaBinary<InputT, OutputT>
    extends BiFunction<InputT, SequentialBinaryBuilder, Computation<OutputT>> {

}
