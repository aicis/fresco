package dk.alexandra.fresco.decimal;

import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import java.util.function.Function;

public interface RealNumericProvider extends Function<ProtocolBuilderNumeric, RealNumeric> {

}
