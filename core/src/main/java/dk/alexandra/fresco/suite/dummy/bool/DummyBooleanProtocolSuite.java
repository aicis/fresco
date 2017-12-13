package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.suite.ProtocolSuiteBinary;


/**
 * Dummy protocol suite that does no secret computation. Only for testing purposes.
 *
 * <b>NB: Do NOT use in production!</b>
 */
public class DummyBooleanProtocolSuite
    implements ProtocolSuiteBinary<ResourcePoolImpl> {

  @Override
  public RoundSynchronization<ResourcePoolImpl> createRoundSynchronization() {
    return new DummyRoundSynchronization<>();
  }

  @Override
  public BuilderFactoryBinary init(ResourcePoolImpl resourcePool,
      Network network) {
    return new DummyBooleanBuilderFactory();
  }


}
