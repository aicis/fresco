package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.StrictBitVector;

/**
 * Implementation for testing and proof-of-concept protocols. This class carries
 * out an INSECURE OT.
 * 
 * @author jot2re
 *
 */
public class DummyOt implements Ot {

  private Integer otherId;
  private Network network;

  /**
   * Construct an insecure dummy OT object based on a real network.
   * 
   * @param otherId
   *          The ID of the other party
   * @param network
   *          The network to use
   */
  public DummyOt(Integer otherId, Network network) {
    super();
    this.otherId = otherId;
    this.network = network;
  }

  @Override
  public StrictBitVector receive(Boolean choiceBit) {
    byte[] messageZeroRaw = this.network.receive(this.otherId);
    byte[] messageOneRaw = this.network.receive(this.otherId);
    return !choiceBit
        ? new StrictBitVector(messageZeroRaw, messageZeroRaw.length * 8)
        : new StrictBitVector(messageOneRaw, messageOneRaw.length * 8);
  }

  @Override
  public void send(StrictBitVector messageZero, StrictBitVector messageOne) {
    this.network.send(otherId, messageZero.toByteArray());
    this.network.send(otherId, messageOne.toByteArray());
  }

}