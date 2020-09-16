package dk.alexandra.fresco.lib.common.compare;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.common.compare.bool.BinaryGreaterThan;
import dk.alexandra.fresco.lib.common.compare.bool.eq.BinaryEquality;
import java.util.List;

/**
 * Default way of producing the protocols within the interface. This default class can be
 * overwritten when implementing {@link BuilderFactoryBinary} if the protocol suite has a better
 * and more efficient way of constructing the protocols.
 */
public class DefaultBinaryComparison implements BinaryComparison {

  private final ProtocolBuilderBinary builder;

  public DefaultBinaryComparison(ProtocolBuilderBinary builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SBool> greaterThan(List<DRes<SBool>> inLeft,
      List<DRes<SBool>> inRight) {
    return this.builder.seq(new BinaryGreaterThan(inLeft, inRight));
  }

  @Override
  public DRes<SBool> equal(List<DRes<SBool>> inLeft,
      List<DRes<SBool>> inRight) {
    return this.builder.seq(new BinaryEquality(inLeft, inRight));
  }

}
