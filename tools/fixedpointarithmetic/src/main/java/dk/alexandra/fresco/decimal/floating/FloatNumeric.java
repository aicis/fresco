package dk.alexandra.fresco.decimal.floating;

import dk.alexandra.fresco.decimal.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.RealNumeric;
import dk.alexandra.fresco.decimal.DefaultLinearAlgebra;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class FloatNumeric implements RealNumeric {

  private ProtocolBuilderNumeric builder;
  private BasicRealNumeric numeric;
  private DefaultAdvancedRealNumeric advanced;
  private DefaultLinearAlgebra linalg;

  public FloatNumeric(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public BasicRealNumeric numeric() {
    if (this.numeric == null) {
      this.numeric = new BasicFloatNumeric(builder);
    }
    return this.numeric;
  }

  @Override
  public DefaultLinearAlgebra linalg() {
    if (this.linalg == null) {
      this.linalg = new FloatLinearAlgebra(builder);
    }
    return this.linalg;
  }

  @Override
  public DefaultAdvancedRealNumeric advanced() {
    if (this.advanced == null) {
      this.advanced = new AdvancedFloatNumeric(builder);
    }
    return this.advanced;
  }


}
