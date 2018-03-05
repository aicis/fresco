package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.decimal.DefaultLinearAlgebra;
import dk.alexandra.fresco.decimal.RealNumeric;
import dk.alexandra.fresco.decimal.fixed.utils.Truncate;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.math.integer.binary.RightShift;

/**
 * This implementation of {@link RealNumeric} allows numbers to be represented as binary fixed point
 * numbers, eg. as <i>m 2<sup>-n</sup></i> for integers <i>m, n</i> with <i>n &ge; 0</i>. This
 * allows a number <i>x</i> to be represented by a fixed point number <i>y = f(x) 2<sup>-n</sup></i>
 * s.t. <i>|x-y| &le; 2<sup>-(n+1)</sup></i>.
 * 
 * For performance reasons, we use the {@link Truncate} algorithm instead of {@link RightShift}, so
 * every time this is done there is a propability that the result will be one larger than the
 * expected value.
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
