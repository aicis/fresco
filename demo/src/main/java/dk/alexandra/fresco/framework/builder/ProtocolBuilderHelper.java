package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.BuilderFactory;

public class ProtocolBuilderHelper {

  public static BuilderFactory getFactory(ProtocolBuilder protocolBuilder) {
    return ((ProtocolBuilderNumeric) protocolBuilder).factory;
  }
}