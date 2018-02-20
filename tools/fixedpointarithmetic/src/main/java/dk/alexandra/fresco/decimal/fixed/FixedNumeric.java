package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.decimal.DefaultLinearAlgebra;
import dk.alexandra.fresco.decimal.RealNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

public class FixedNumeric implements RealNumeric {

  private final ProtocolBuilderNumeric builder;
  private BasicRealNumeric numeric;
  private DefaultAdvancedRealNumeric advanced;
  private DefaultLinearAlgebra linalg;
  private final Base base;

  public enum Base { BINARY, DECIMAL };
  
  public FixedNumeric(ProtocolBuilderNumeric builder, Base base) {
    this.builder = builder;
    this.base = base;
  }
  
  public FixedNumeric(ProtocolBuilderNumeric builder) {
    this(builder, Base.BINARY);
  }

  @Override
  public BasicRealNumeric numeric() {
    if (this.numeric == null) {
      if (base == Base.BINARY) {
        this.numeric = new BinaryBasicFixedNumeric(builder);
      } else if (base == Base.DECIMAL) {
        this.numeric = new DecimalBasicFixedNumeric(builder);
      }
    }
    return this.numeric;
  }

  @Override
  public DefaultLinearAlgebra linalg() {
    if (this.linalg == null) {
      this.linalg = new FixedLinearAlgebra(builder, base);
    }
    return this.linalg;
  }

  @Override
  public DefaultAdvancedRealNumeric advanced() {
    if (this.advanced == null) {
      this.advanced = new AdvancedFixedNumeric(builder, base);
    }
    return this.advanced;
  }


}
