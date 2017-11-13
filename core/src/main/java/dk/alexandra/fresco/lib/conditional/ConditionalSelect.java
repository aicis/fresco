package dk.alexandra.fresco.lib.conditional;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Chooses left or right based on the selection bit. Equivalent to the java expression:
 * <code>selector ? left : right<code>
 */
public class ConditionalSelect implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private final DRes<SInt> condition;

  public ConditionalSelect(DRes<SInt> selector, DRes<SInt> left, DRes<SInt> right) {
    this.condition = selector;
    this.left = left;
    this.right = right;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    Numeric numeric = builder.numeric();
    DRes<SInt> sub = numeric.sub(left, right);
    DRes<SInt> mult = numeric.mult(condition, sub);
    return numeric.add(mult, right);
  }
}
