package dk.alexandra.fresco.tools.mascot.broadcast;

import dk.alexandra.fresco.framework.network.Network;
import java.util.Arrays;
import java.util.List;

/**
 * A network proxy which provides actively-secure broadcast functionality. Hash-based broadcast
 * validation is performed on all received messages.
 */
public class BroadcastingNetworkProxy implements Network {

  private final Network network;
  private final BroadcastValidation validator;

  /**
   * Decorates a regular network with actively secure broadcast functionality.
   * 
   * @param network raw network
   * @param validator the broadcast validation protocol
   */
  public BroadcastingNetworkProxy(Network network, BroadcastValidation validator) {
    if (network.getNoOfParties() < 3) {
      throw new IllegalArgumentException("Broadcast only needed for three or more parties");
    }
    this.network = network;
    this.validator = validator;
  }

  /**
   * Point-to-point networking is not supported for broadcast.
   */
  @Override
  public void send(int partyId, byte[] data) {
    throw new UnsupportedOperationException("Broadcast network can only send to all");
  }

  /**
   * Runs broadcast validation upon receiving messages to ensure consistency.
   */
  @Override
  public byte[] receive(int partyId) {
    byte[] received = network.receive(partyId);
    validator.validate(Arrays.asList(received));
    return received;
  }

  @Override
  public int getNoOfParties() {
    return network.getNoOfParties();
  }

  @Override
  public void sendToAll(byte[] data) {
    network.sendToAll(data);
  }

  /**
   * Runs broadcast validation upon receiving messages to ensure consistency.
   */
  @Override
  public List<byte[]> receiveFromAll() {
    List<byte[]> received = network.receiveFromAll();
    validator.validate(received);
    return received;
  }

}
