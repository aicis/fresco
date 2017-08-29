package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.compare.bool.BinaryGreaterThan;
import dk.alexandra.fresco.lib.compare.bool.eq.BinaryEqualityProtocol;
import java.util.List;

public class DefaultComparisonBinaryBuilder implements ComparisonBuilderBinary {

  private final ProtocolBuilderBinary builder;

  protected DefaultComparisonBinaryBuilder(ProtocolBuilderBinary builder) {
    this.builder = builder;
  }

  @Override
  public Computation<SBool> greaterThan(List<Computation<SBool>> inLeft,
      List<Computation<SBool>> inRight) {
    return this.builder.seq(new BinaryGreaterThan(inLeft, inRight));
  }

  @Override
  public Computation<SBool> equal(List<Computation<SBool>> inLeft,
      List<Computation<SBool>> inRight) {
    return this.builder.seq(new BinaryEqualityProtocol(inLeft, inRight));
  }

}
