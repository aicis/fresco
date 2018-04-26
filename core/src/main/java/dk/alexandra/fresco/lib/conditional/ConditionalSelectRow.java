package dk.alexandra.fresco.lib.conditional;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * Does a conditional select on two lists rather than on two numbers. Equivalent to the java
 * expression: <code>condition ? left : right</code>
 */
public class ConditionalSelectRow<T extends DRes<SInt>>
    implements ComputationParallel<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<SInt> condition;
  private final DRes<List<T>> left;
  private final DRes<List<T>> right;

  public ConditionalSelectRow(DRes<SInt> selector,
      DRes<List<T>> left,
      DRes<List<T>> right) {
    // TODO: throw if different sizes
    this.condition = selector;
    this.left = left;
    this.right = right;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SInt>> selected = new ArrayList<>();
    List<? extends DRes<SInt>> leftOut = left.out();
    List<? extends DRes<SInt>> rightOut = right.out();
    AdvancedNumeric advancedNumeric = builder.advancedNumeric();
    for (int i = 0; i < leftOut.size(); i++) {
      selected.add(advancedNumeric.condSelect(condition, leftOut.get(i), rightOut.get(i)));
    }
    return () -> selected;
  }
}
