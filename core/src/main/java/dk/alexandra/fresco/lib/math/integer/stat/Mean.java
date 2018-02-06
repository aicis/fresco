package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;

/**
 * This protocol calculates the arithmetic mean of a data set.
 */
public class Mean implements Computation<SInt, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> data;
  private final int degreesOfFreedom;

  public Mean(List<DRes<SInt>> data) {
    this(data, data.size());
  }

  public Mean(List<DRes<SInt>> data, int degreesOfFreedom) {
    this.data = data;
    this.degreesOfFreedom = degreesOfFreedom;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq((seq) ->
        () -> this.data
    ).seq((seq, list) -> seq.advancedNumeric().sum(list)
    ).seq((seq, sum) -> {
      BigInteger n = BigInteger.valueOf(this.degreesOfFreedom);
      return seq.advancedNumeric().div(() -> sum, n);
    });
  }

}
