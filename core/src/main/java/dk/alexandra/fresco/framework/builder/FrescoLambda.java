package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import java.util.function.BiFunction;

public interface FrescoLambda<InputT, OutputT> extends
    BiFunction<InputT, SequentialNumericBuilder, Computation<OutputT>> {

}
