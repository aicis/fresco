package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.RealNumeric;
import dk.alexandra.fresco.decimal.DefaultLinearAlgebra;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class FixedNumeric implements RealNumeric {

  private ProtocolBuilderNumeric builder;
  private int precision;
  private BasicRealNumeric numeric;
  private DefaultAdvancedRealNumeric advanced;
  private DefaultLinearAlgebra linalg;

  public FixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.precision = precision;
  }

  @Override
  public BasicRealNumeric numeric() {
    if (this.numeric == null) {
      this.numeric = new BasicFixedNumeric(builder, precision);
    }
    return this.numeric;
  }

  @Override
  public DefaultAdvancedRealNumeric advanced() {
    if (this.advanced == null) {
      this.advanced = new AdvancedFixedNumeric(builder, precision);
    }
    return this.advanced;
  }

  @Override
  public DefaultLinearAlgebra linalg() {
    if (this.linalg == null) {
      this.linalg = new FixedLinearAlgebra(builder, precision);
    }
    return this.linalg;
  }


}
