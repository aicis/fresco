package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.suite.ProtocolSuite;


/**
 * Dummy protocol suite that does no secret computation. Only for testing purposes.
 *
 * <b>NB: Do NOT use in production!</b>
 */
public class DummyBooleanProtocolSuite
    implements ProtocolSuite<ResourcePoolImpl, ProtocolBuilderBinary> {

  @Override
  public RoundSynchronization<ResourcePoolImpl> createRoundSynchronization() {
    return new DummyRoundSynchronization<>();
  }

  @Override
  public BuilderFactory<ProtocolBuilderBinary> init(ResourcePoolImpl resourcePool,
      Network network) {
    BuilderFactory<ProtocolBuilderBinary> b = new DummyBooleanBuilderFactory();
    return b;
  }


}
