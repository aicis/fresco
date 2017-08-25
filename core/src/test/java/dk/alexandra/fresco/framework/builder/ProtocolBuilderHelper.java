package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;

public class ProtocolBuilderHelper {

  public static BuilderFactory getFactoryNumeric(ProtocolBuilderNumeric protocolBuilder) {
    return protocolBuilder.getFactory();
  }

  public static BuilderFactory getFactoryBinary(ProtocolBuilderBinary protocolBuilder) {
    return protocolBuilder.getFactory();
  }
}