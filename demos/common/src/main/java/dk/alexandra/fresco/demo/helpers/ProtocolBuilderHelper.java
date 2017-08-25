package dk.alexandra.fresco.demo.helpers;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class ProtocolBuilderHelper {

  public static BuilderFactory getNumericFactory(ProtocolBuilder protocolBuilder) {
    return ((ProtocolBuilderNumeric) protocolBuilder).getFactory();
  }
  
  public static BuilderFactory getBinaryFactory(ProtocolBuilder protocolBuilder) {
    return ((ProtocolBuilderBinary) protocolBuilder).getFactory();
  }
}