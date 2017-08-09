package dk.alexandra.fresco.demo.helpers;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;

public class ProtocolBuilderHelper {

  public static BuilderFactory getNumericFactory(ProtocolBuilder protocolBuilder) {
    return ((ProtocolBuilderNumeric) protocolBuilder).getFactory();
  }
  
  public static BuilderFactory getBinaryFactory(ProtocolBuilder protocolBuilder) {
    return ((ProtocolBuilderBinary) protocolBuilder).factory;
  }
}