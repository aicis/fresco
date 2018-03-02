package dk.alexandra.fresco.decimal;

import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import java.util.function.Function;

/**
 * Provider function which gives an instance of a {@link RealNumeric} based on a
 * {@link ProtocolNumericBuilder}.
 */
public interface RealNumericProvider extends Function<ProtocolBuilderNumeric, RealNumeric> {

}
