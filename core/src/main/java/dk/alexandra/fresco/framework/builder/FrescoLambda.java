package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import java.util.function.BiFunction;

public interface FrescoLambda<InputT, OutputT> extends
    BiFunction<InputT, SequentialProtocolBuilder, Computation<OutputT>> {

}
