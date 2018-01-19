package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.field.FieldElementUtils;

/**
 * Class that stores data and resources common across all MPC protocols.
 */
public abstract class BaseProtocol {

  private final MascotResourcePool resourcePool;
  private final Network network;
  private final FieldElementUtils fieldElementUtils;

  /**
   * Creates new {@link BaseProtocol}.
   *
   * @param resourcePool mascot resource pool
   * @param network network
   */
  public BaseProtocol(MascotResourcePool resourcePool, Network network) {
    this.resourcePool = resourcePool;
    this.network = network;
    this.fieldElementUtils = new FieldElementUtils(resourcePool.getModulus());
  }

  protected FieldElementUtils getFieldElementUtils() {
    return fieldElementUtils;
  }

  protected Network getNetwork() {
    return network;
  }

  protected MascotResourcePool getResourcePool() {
    return resourcePool;
  }
}
