package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.ParallelProtocolBuilder;
import java.util.function.BiFunction;

public interface FrescoLambdaParallel<InputT, OutputT> extends
    BiFunction<InputT, ParallelProtocolBuilder, Computation<OutputT>> {

}
