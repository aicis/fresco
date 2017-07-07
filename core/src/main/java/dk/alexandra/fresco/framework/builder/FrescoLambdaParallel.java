package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.ParallelNumericBuilder;
import java.util.function.BiFunction;

public interface FrescoLambdaParallel<InputT, OutputT> extends
    BiFunction<InputT, ParallelNumericBuilder, Computation<OutputT>> {

}
