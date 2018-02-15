package dk.alexandra.fresco.decimal.floating;

import dk.alexandra.fresco.decimal.DefaultLinearAlgebra;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class FloatLinearAlgebra extends DefaultLinearAlgebra {

  public FloatLinearAlgebra(ProtocolBuilderNumeric builder) {
    super(builder, scope -> new FloatNumeric(scope));
  }

}
