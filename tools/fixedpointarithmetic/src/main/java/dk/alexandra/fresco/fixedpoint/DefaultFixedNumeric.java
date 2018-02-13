package dk.alexandra.fresco.fixedpoint;

import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class DefaultFixedNumeric implements FixedNumeric {

  private ProtocolBuilderNumeric builder;
  private int precision;
  private BasicFixedNumeric numeric;
  private AdvancedFixedNumeric advanced;
  private LinearAlgebra linalg;

  public DefaultFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.precision = precision;
  }

  @Override
  public BasicFixedNumeric numeric() {
    if (this.numeric == null) {
      this.numeric = new SIntWrapperFixedNumeric(builder, precision);
    }
    return this.numeric;
  }

  @Override
  public AdvancedFixedNumeric advanced() {
    if (this.advanced == null) {
      this.advanced = new SIntWrapperAdvancedFixedNumeric(builder, precision);
    }
    return this.advanced;
  }

  @Override
  public LinearAlgebra linalg() {
    if (this.linalg == null) {
      this.linalg = new SIntWrapperLinearAlgebra(builder, precision);
    }
    return this.linalg;
  }


}
