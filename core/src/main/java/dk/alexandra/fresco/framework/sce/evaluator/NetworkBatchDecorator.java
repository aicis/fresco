package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteAndBitConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Default network for the evaluators, this interface bridges the raw network4 with the fresco
 * default evaluators. This class makes the communication on the network batched and hence throttled
 * so evaluators behave nice on the network. <br/> It is important to call flush to empty all
 * buffers after sending and before receiving data
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
    boolean isLargePacket = (count & 0x80) == 0x80;
    if (!isLargePacket) {
      byte[] bytes = new byte[count];
      byteInputStream.read(bytes, 0, count);
      return bytes;
    } else {
      byte[] lengthBytes = new byte[4];
      lengthBytes[0] = (byte) (count & 0x7f); // zero out indicator bit
      byteInputStream.read(lengthBytes, 1, 3);
      int numBytes = ByteAndBitConverter.toInt(lengthBytes, 0);
      byte[] bytes = new byte[numBytes];
      byteInputStream.read(bytes, 0, numBytes);
      return bytes;
    }
  }

  @Override
  public int getNoOfParties() {
    return noOfParties;
  }

  @Override
  public void send(int id, byte[] data) {
    ByteArrayOutputStream buffer = this.output
        .computeIfAbsent(id, (i) -> new ByteArrayOutputStream());
    final int packetLength = data.length;
    // we need the top bit to be free because we use it as an indicator for the packet type
    if (packetLength <= 0x7f) {
      // small packet case
      // set msb in byte to 0 to indicate small packet
      byte packetLengthAsByte = (byte) (packetLength & 0x7f);
      buffer.write(packetLengthAsByte);
      buffer.write(data, 0, packetLengthAsByte);
    } else {
      // large packet case
      // we need the top bit to be free because we use it as an indicator for the packet type
      if (packetLength >= 0x7fffffff) {
        throw new IllegalStateException(
            "Current implementation only supports small packages, data.length=" + packetLength);
      }
      // set top bit to 1 to indicate large packet
      int withSetBit = packetLength | 0x80000000;
      final byte[] b = ByteAndBitConverter.toByteArray(withSetBit);
      buffer.write(b, 0, 4);
      buffer.write(data, 0, packetLength);
    }
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
