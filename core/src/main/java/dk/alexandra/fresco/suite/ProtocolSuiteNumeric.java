package dk.alexandra.fresco.suite;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;

/**
 * Marker interface for numeric protocol suite with the builder bound to the type of numeric.
 * This forces implementors of numeric suites to inherit from this common interface - and
 * therefore can have their behaviour changed via e.g. decorators.
 *
 * @param <ResourcePoolT> the resource pool type.
 */
public interface ProtocolSuiteNumeric<ResourcePoolT extends NumericResourcePool> extends
    ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric> {

  @Override
  BuilderFactoryNumeric init(ResourcePoolT resourcePool, Network network);
}
