package dk.alexandra.fresco.lib.real.fixed.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;

public class FixedCondSelect implements Computation<SReal, ProtocolBuilderNumeric> {

  private DRes<SInt> condition;
  private SFixed first, second;

  public FixedCondSelect(DRes<SInt> condition, SFixed first, SFixed second) {
    this.condition = condition;
    this.first = first;
    this.second = second;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return new SFixed(
        builder.advancedNumeric().condSelect(condition, first.getSInt(), second.getSInt()));
  }

}
