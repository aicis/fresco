package dk.alexandra.fresco.suite;

import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

/**
 * Marker interface for binary protocol suite with the builder bound to the type of binary.
 * This forces implementors of binary suites to inherit from this common interface - and
 * therefore can have their behaviour changed via e.g. decorators.
 *
 * @param <ResourcePoolT> the resource pool type.
 */
public interface ProtocolSuiteBinary<ResourcePoolT extends ResourcePool> extends
    ProtocolSuite<ResourcePoolT, ProtocolBuilderBinary> {

  @Override
  BuilderFactoryBinary init(ResourcePoolT resourcePool, Network network);
}
