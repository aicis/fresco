package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.DefaultLinearAlgebra;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class FixedLinearAlgebra extends DefaultLinearAlgebra {

  public FixedLinearAlgebra(ProtocolBuilderNumeric builder) {
    super(builder, scope -> new FixedNumeric(scope));
  }

}
