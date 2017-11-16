package dk.alexandra.fresco.framework.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SCENetworkImpl implements SCENetwork {

  private int noOfParties;
  private final Network network;
  private Map<Integer, List<byte[]>> output;

  public SCENetworkImpl(int noOfParties, Network network) {
    this.noOfParties = noOfParties;
    this.network = network;
    this.output = new HashMap<>();
  }

  @Override
  public byte[] receive(int id) {
    return network.receive(id);
  }

  @Override
  public List<byte[]> receiveFromAll() {
    List<byte[]> res = new ArrayList<>();
    for (int i = 1; i <= noOfParties; i++) {
      res.add(receive(i));
    }
    return res;
  }


  @Override
  public void send(int id, byte[] data) {
    List<byte[]> buffer =
        this.output.computeIfAbsent(id, k -> new ArrayList<>());
    buffer.add(data);
  }

  @Override
  public void sendToAll(byte[] data) {
    for (int i = 1; i <= noOfParties; i++) {
      send(i, data);
    }
  }

  @Override
  public void flushBuffer() {
    for (Integer partyId : output.keySet()) {
      List<byte[]> bytes = output.get(partyId);
      for (byte[] data : bytes) {
        network.send(partyId, data);
      }
    }
    this.output.clear();
  }
}
