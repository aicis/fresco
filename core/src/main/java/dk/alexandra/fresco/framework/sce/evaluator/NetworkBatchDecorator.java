package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.network.Network;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Default network for the evaluators, this interface bridges the raw network4
 * with the fresco default evaluators. This class makes the
 * communication on the network batched and hence throttled so evaluators behave nice
 * on the network.
 * <br/>
 * It is important to call flush to empty all buffers after sending and before receiving data
 */
public class NetworkBatchDecorator implements Network {

  private int noOfParties;
  private final Network network;
  private Map<Integer, ByteArrayOutputStream> output;
  private Map<Integer, ByteArrayInputStream> input;

  public NetworkBatchDecorator(int noOfParties, Network network) {
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
  public int getNoOfParties() {
    return noOfParties;
  }

  @Override
  public void send(int id, byte[] data) {
    ByteArrayOutputStream buffer = this.output
        .computeIfAbsent(id, (i) -> new ByteArrayOutputStream());
    if (data.length > Byte.MAX_VALUE) {
      throw new IllegalStateException(
          "Current implementation only supports small packages, data.length=" + data.length);
    }
    buffer.write(data.length);
    buffer.write(data, 0, data.length);
  }

  /**
   * Flushes the internal buffers and sends the (remaining) pieces over the wire.
   */
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
