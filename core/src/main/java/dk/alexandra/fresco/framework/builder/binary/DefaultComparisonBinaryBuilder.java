package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.compare.bool.BinaryGreaterThanProtocolImpl;
import dk.alexandra.fresco.lib.compare.bool.eq.BinaryEqualityProtocolImpl;
import java.util.List;

public class DefaultComparisonBinaryBuilder implements ComparisonBuilderBinary {

  private final ProtocolBuilderBinary builder;

  public DefaultComparisonBinaryBuilder(ProtocolBuilderBinary builder) {
    super();
    this.builder = builder;
  }

  @Override
  public Computation<SBool> greaterThan(List<Computation<SBool>> inLeft,
      List<Computation<SBool>> inRight) {
    return this.builder.seq(new BinaryGreaterThanProtocolImpl(inLeft, inRight));
  }

  @Override
  public Computation<SBool> equal(List<Computation<SBool>> inLeft,
      List<Computation<SBool>> inRight) {
    return this.builder.seq(new BinaryEqualityProtocolImpl(inLeft, inRight));
  }

}
