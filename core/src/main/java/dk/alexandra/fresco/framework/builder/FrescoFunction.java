package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import java.util.function.Function;

public interface FrescoFunction<OutputT> extends
    Function<SequentialProtocolBuilder, Computation<OutputT>> {
}
