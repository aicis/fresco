package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import java.util.function.BiFunction;

public interface FrescoLambdaParallel<InputT, ParallelNumericBuilder, OutputT> extends
    BiFunction<InputT, ParallelNumericBuilder, Computation<OutputT>> {

}
