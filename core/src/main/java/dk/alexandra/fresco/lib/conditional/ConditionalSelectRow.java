package dk.alexandra.fresco.lib.conditional;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * Does a conditional select on two lists rather than on two numbers. Equivalent to the java
 * expression: <code>condition ? left : right</code>
 */
public class ConditionalSelectRow
    implements ComputationParallel<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<SInt> condition;
  private final DRes<List<DRes<SInt>>> left;
  private final DRes<List<DRes<SInt>>> right;

  public ConditionalSelectRow(DRes<SInt> selector, DRes<List<DRes<SInt>>> left,
      DRes<List<DRes<SInt>>> right) {
    // TODO: throw if different sizes
    this.condition = selector;
    this.left = left;
    this.right = right;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SInt>> selected = new ArrayList<>();
    List<DRes<SInt>> leftOut = left.out();
    List<DRes<SInt>> rightOut = right.out();
    for (int i = 0; i < leftOut.size(); i++) {
      selected
          .add(builder.advancedNumeric().condSelect(condition, leftOut.get(i), rightOut.get(i)));
    }
    return () -> selected;
  }
}
