package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import java.util.function.BiFunction;

/**
 * Any computation in fresco that is composite by nature - and hence requires a specific
 * type of builder.
 * @param <InputT> the input of this function
 * @param <BuilderT> the type of builder of the composition
 * @param <OutputT> the output of the function
 */
public interface FrescoLambda<
    InputT,
    BuilderT extends ProtocolBuilder,
    OutputT
    > extends BiFunction<InputT, BuilderT, Computation<OutputT>> {

}
