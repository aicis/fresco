package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.decimal.DefaultLinearAlgebra;
import dk.alexandra.fresco.decimal.RealNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;

/**
 * This implementation of {@link RealNumeric} allows numbers to be represented as binary fixed point
 * numbers, eg. as <i>m 2<sup>-n</sup></i> for integers <i>m, n</i> with <i>0 &le; n &le; N</i> for
 * some upper bound <i>N</i>. Inputs are converted to such a representation for a given default
 * precision (see {@link #FixedNumeric(ProtocolBuilderNumeric, int)}), but intermediate results and
 * outputs may be represented with higher precision.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class FixedNumeric implements RealNumeric {

  private final ProtocolBuilderNumeric builder;
  private BasicRealNumeric numeric;
  private DefaultAdvancedRealNumeric advanced;
  private DefaultLinearAlgebra linalg;
  private final int precision;

  public FixedNumeric(ProtocolBuilderNumeric builder) {
    this(builder, 16);
  }

  /**
   * Create a new {@link FixedNumeric} that interprets inputs as fixed point numbers with the given
   * precision. Intermediate calculations and outputs will be represented as fixed point numbers
   * with at least the same precision.
   * 
   * @param builder
   * @param precision
   */
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
