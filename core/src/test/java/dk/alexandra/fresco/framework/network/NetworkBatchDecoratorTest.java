package dk.alexandra.fresco.framework.network;

import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NetworkBatchDecoratorTest {

  private NetworkBatchDecorator networkBatchDecorator;
  private Map<Integer, byte[]> transmissions = new HashMap<>();

  @Before
  public void setUp() throws Exception {
    DummyNetwork network = new DummyNetwork();
    networkBatchDecorator = new NetworkBatchDecorator(3, network);
  }

  @Test
  public void receive() throws Exception {
    transmissions.put(1, new byte[]{4, 2, 2, 23, 3, 3, 22, 0, 0});
    transmissions.put(2, new byte[]{0});
    Assert.assertArrayEquals(new byte[]{2, 2, 23, 3}, networkBatchDecorator.receive(1));
    Assert.assertArrayEquals(new byte[]{}, networkBatchDecorator.receive(2));
    Assert.assertArrayEquals(new byte[]{22, 0, 0}, networkBatchDecorator.receive(1));
  }

  @Test
  public void receiveFromAll() throws Exception {
    transmissions.put(1, new byte[]{4, 2, 2, 23, 3, 42});
    transmissions.put(2, new byte[]{4, 2, 2, 23, 3, 42});
    transmissions.put(3, new byte[]{4, 2, 2, 23, 3, 42});
    List<byte[]> receiveAll = networkBatchDecorator.receiveFromAll();
    for (byte[] receive : receiveAll) {
      Assert.assertArrayEquals(new byte[]{2, 2, 23, 3}, receive);
    }
  }

  @Test(expected = NullPointerException.class)
  public void receiveFromAllNoData() throws Exception {
    transmissions.put(1, new byte[]{4, 2, 2, 23, 3, 42});
    transmissions.put(2, new byte[]{4, 2, 2, 23, 3, 42});
    networkBatchDecorator.receiveFromAll();
  }

  @Test
  public void send() throws Exception {
    networkBatchDecorator.send(1, new byte[]{123});
    Assert.assertTrue(transmissions.isEmpty());
    networkBatchDecorator.flush();
    Assert.assertEquals(1, transmissions.size());
    Assert.assertArrayEquals(new byte[]{1, 123}, transmissions.get(1));
  }

  @Test
  public void sendToAll() throws Exception {
    networkBatchDecorator.sendToAll(new byte[]{123});
    networkBatchDecorator.flush();
    Assert.assertEquals(3, transmissions.size());
    Assert.assertArrayEquals(new byte[]{1, 123}, transmissions.get(1));
    Assert.assertArrayEquals(new byte[]{1, 123}, transmissions.get(2));
    Assert.assertArrayEquals(new byte[]{1, 123}, transmissions.get(3));
  }

  @Test(expected = RuntimeException.class)
  public void errorOnBigPackets() throws Exception {
    networkBatchDecorator.sendToAll(
        new byte[]{
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
            123, 123, 123, 123, 123, 123, 123, 123, 123, 123,
        });

  }

  private class DummyNetwork implements Network {


    @Override
    public void send(int partyId, byte[] data) {
      transmissions.put(partyId, data);
    }

    @Override
    public byte[] receive(int partyId) {
      return transmissions.get(partyId);
    }

    @Override
    public int getNoOfParties() {
      return 3;
    }
  }
}