package dk.alexandra.fresco.decimal.floating.binary;

import dk.alexandra.fresco.decimal.DefaultLinearAlgebra;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class BinaryFloatLinearAlgebra extends DefaultLinearAlgebra {

  public BinaryFloatLinearAlgebra(ProtocolBuilderNumeric builder) {
    super(builder, scope -> new BinaryFloatNumeric(scope));
  }

}
