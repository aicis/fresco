package dk.alexandra.fresco.demo.helpers;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;

public class ProtocolBuilderHelper {

  public static BuilderFactory getNumericFactory(ProtocolBuilder protocolBuilder) {
    return ((ProtocolBuilderNumeric) protocolBuilder).factory;
  }
  
  public static BuilderFactory getBinaryFactory(ProtocolBuilder protocolBuilder) {
    return ((ProtocolBuilderBinary) protocolBuilder).factory;
  }
}