package dk.alexandra.fresco.framework.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation that postpones every bit of communication until the end of the round.
 */
public class SCENetworkImpl implements SCENetwork {

  private int noOfParties;
  private final Network network;
  private Map<Integer, ByteArrayOutputStream> output;
  private Map<Integer, ByteArrayInputStream> input;

  public SCENetworkImpl(int noOfParties, Network network) {
    this.noOfParties = noOfParties;
    this.network = network;
    this.output = new HashMap<>();
    this.input = new HashMap<>();
  }

  @Override
  public byte[] receive(int id) {
    ByteArrayInputStream byteInputStream = input.get(id);
    if (byteInputStream == null) {
      byte[] partyData = network.receive(id);
      byteInputStream = new ByteArrayInputStream(partyData);
      input.put(id, byteInputStream);
    }

    int count = byteInputStream.read();
    byte[] bytes = new byte[count];
    byteInputStream.read(bytes, 0, count);
    return bytes;
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
    ByteArrayOutputStream buffer = this.output
        .computeIfAbsent(id, (i) -> new ByteArrayOutputStream());
    buffer.write(data.length);
    buffer.write(data, 0, data.length);
  }

  @Override
  public void sendToAll(byte[] data) {
    for (int i = 1; i <= noOfParties; i++) {
      send(i, data);
    }
  }

  @Override
  public void flush() {
    for (int i = 1; i <= noOfParties; i++) {
      if (output.containsKey(i)) {
        ByteArrayOutputStream byteArrayOutputStream = output.get(i);
        byte[] data = byteArrayOutputStream.toByteArray();
        network.send(i, data);
      }
      output.remove(i);
    }
    input.clear();
  }
}
