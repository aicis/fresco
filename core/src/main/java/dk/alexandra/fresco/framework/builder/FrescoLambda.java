package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import java.util.function.BiFunction;

public interface FrescoLambda<
    InputT,
    SequentialBuilderT extends ProtocolBuilder<SequentialBuilderT>,
    OutputT
    > extends BiFunction<InputT, SequentialBuilderT, Computation<OutputT>> {

}
