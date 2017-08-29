package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class ProtocolBuilderHelper {

  public static BuilderFactory getFactoryNumeric(ProtocolBuilderNumeric protocolBuilder) {
    return protocolBuilder.getFactory();
  }

}
