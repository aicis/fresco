package dk.alexandra.fresco.lib.fixed.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

/**
 * This computation returns the first value if the condition varibale is one and the second if the
 * condition variable is zero.
 */
public class FixedCondSelect implements Computation<SFixed, ProtocolBuilderNumeric> {

  private DRes<SInt> condition;
  private SFixed first, second;

  public FixedCondSelect(DRes<SInt> condition, SFixed first, SFixed second) {
    this.condition = condition;
    this.first = first;
    this.second = second;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return new SFixed(
        AdvancedNumeric.using(builder).condSelect(condition, first.getSInt(), second.getSInt()));
  }

}
