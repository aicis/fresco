package dk.alexandra.fresco.tools.mascot.broadcast;

import java.util.List;

import dk.alexandra.fresco.framework.network.Network;

public class BroadcastingNetworkDecorator implements Network {

  Network network;
  BroadcastValidation validator;

  /**
   * Decorates a regular network with actively secure broadcast functionality.
   * 
   * @param network raw network
   * @param validator the broadcast validation protocol
   */
  public BroadcastingNetworkDecorator(Network network, BroadcastValidation validator) {
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
   * Point-to-point networking is not supported for broadcast.
   */
  @Override
  public byte[] receive(int partyId) {
    throw new UnsupportedOperationException("Broadcast network can only receive from all");
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
