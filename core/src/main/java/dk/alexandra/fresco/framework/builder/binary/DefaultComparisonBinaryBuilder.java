package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;

public class DefaultComparisonBinaryBuilder implements ComparisonBuilderBinary {

  private final BuilderFactoryBinary factoryBinary;
  private final ProtocolBuilderBinary builder;

  public DefaultComparisonBinaryBuilder(BuilderFactoryBinary factoryBinary,
      ProtocolBuilderBinary builder) {
    super();
    this.factoryBinary = factoryBinary;
    this.builder = builder;
  }

  @Override
  public Computation<SBool> greaterThan(SBool[] inLeft, SBool[] inRight) {
    return null;
    // return this.builder.createSequentialSub(
    // new BinaryGreaterThanProtocolImpl(inLeft, inRight, this.factoryBinary));
  }

  @Override
  public Computation<SBool> equal(SBool[] inLeft, SBool[] inRight) {
    return null;
    // return this.builder
    // .createSequentialSub(new AltBinaryEqualityProtocol(inLeft, inRight, this.factoryBinary));
  }

}
