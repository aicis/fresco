package dk.alexandra.fresco.suite;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

/**
 * Models an arithmetic protocol suite working within a field.
 *
 * @param <ResourcePoolT> The resource pool type to work with. 
 * @param <Builder> The builder type to use.
 */
public interface NumericProtocolSuite<ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder>
    extends ProtocolSuite<ResourcePoolT, Builder> {

  /**
   * Gets an approximation of the maximum bit length of any number appearing in an application. This
   * is used by certain protocols, e.g., to avoid overflow when working in a Z_p field. TODO:
   * Consider factoring out of the configuration as this is really specific only to certain
   * protocols.
   *
   * @return the expected maximum bit length of any number appearing in the application.
   */
  int getMaxBitLength();

  /**
   * 
   * @return The modulus of the group we are simulating
   */
  BigInteger getModulus();
}
