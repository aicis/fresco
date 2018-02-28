package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.decimal.DefaultLinearAlgebra;
import dk.alexandra.fresco.decimal.RealNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

/**
 * This implementation of {@link RealNumeric} allows numbers to be represented as binary fixed point
 * numbers, eg. as <i>x * 2<sup>-n</sup></i> for integers <i>x, n</i> with <i>n &ge; 0</i>.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class FixedNumeric implements RealNumeric {

  private final ProtocolBuilderNumeric builder;
  private BasicRealNumeric numeric;
  private DefaultAdvancedRealNumeric advanced;
  private DefaultLinearAlgebra linalg;

  public FixedNumeric(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public BasicRealNumeric numeric() {
    if (this.numeric == null) {
      this.numeric = new BasicFixedNumeric(builder);
    }
    return this.numeric;
  }

  @Override
  public DefaultLinearAlgebra linalg() {
    if (this.linalg == null) {
      this.linalg = new FixedLinearAlgebra(builder);
    }
    return this.linalg;
  }

  @Override
  public DefaultAdvancedRealNumeric advanced() {
    if (this.advanced == null) {
      this.advanced = new AdvancedFixedNumeric(builder);
    }
    return this.advanced;
  }


}
