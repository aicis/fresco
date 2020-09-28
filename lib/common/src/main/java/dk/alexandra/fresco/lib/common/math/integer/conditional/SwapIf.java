package dk.alexandra.fresco.lib.common.math.integer.conditional;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;

/**
 * Swaps left with right if the swapper bit is true. Does no swapping otherwise.
 */
public class SwapIf
    implements ComputationParallel<Pair<DRes<SInt>, DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<SInt> left;
  private final DRes<SInt> right;
  private final DRes<SInt> swapper;

  public SwapIf(DRes<SInt> swapper, DRes<SInt> left, DRes<SInt> right) {
    this.swapper = swapper;
    this.left = left;
    this.right = right;
  }

  @Override
  public DRes<Pair<DRes<SInt>, DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    AdvancedNumeric advancedNumeric = AdvancedNumeric.using(builder);
    DRes<SInt> updatedA = advancedNumeric.condSelect(swapper, right, left);
    DRes<SInt> updatedB = advancedNumeric.condSelect(swapper, left, right);
    return () -> new Pair<>(updatedA, updatedB);
  }
}
