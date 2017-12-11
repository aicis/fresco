package dk.alexandra.fresco.network;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import edu.biu.scapi.comm.AuthenticatedChannel;
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.EncryptedChannel;
import edu.biu.scapi.comm.PlainChannel;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.comm.twoPartyComm.SocketCommunicationSetup;
import edu.biu.scapi.comm.twoPartyComm.SocketPartyData;
import edu.biu.scapi.comm.twoPartyComm.TwoPartyCommunicationSetup;
import edu.biu.scapi.midLayer.symmetricCrypto.encryption.ScCTREncRandomIV;
import edu.biu.scapi.midLayer.symmetricCrypto.encryption.ScEncryptThenMac;
import edu.biu.scapi.midLayer.symmetricCrypto.mac.Mac;
import edu.biu.scapi.midLayer.symmetricCrypto.mac.ScCbcMacPrepending;
import edu.biu.scapi.primitives.prf.AES;
import edu.biu.scapi.primitives.prf.bc.BcAES;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Before;
import org.junit.Test;


/**
 * Test some basic functionality of SCAPI network layer, independently of fresco.
 */
public class TestScapiNetworkLayer {

  private Map<Integer, NetworkConfiguration> netConfs = new HashMap<>();

  private void runTest(
      TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test, int n) {
    // Since SCAPI currently does not work with ports > 9999 we use fixed ports
    // here instead of relying on ephemeral ports which are often > 9999.
    List<Integer> ports = new ArrayList<>(n);
    for (int i = 1; i <= n; i++) {
      ports.add(9000 + i);
    }
    Map<Integer, NetworkConfiguration> netConf =
        TestConfiguration.getNetworkConfigurations(n, ports);
    Map<Integer, TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric>> conf =
        new HashMap<>();
    for (int i : netConf.keySet()) {
      ScapiNetworkImpl network = new ScapiNetworkImpl();
      network.init(netConf.get(i), 1);
      TestThreadConfiguration<ResourcePoolImpl, ProtocolBuilderNumeric> ttc =
          new TestThreadConfiguration<>(null,
              () -> new ResourcePoolImpl(i, n, null), () -> network);
      conf.put(i, ttc);
      netConfs.put(i, netConf.get(i));
    }
    TestThreadRunner.run(test, conf);

  }

  @Before
  public void clear() {
    netConfs.clear();
  }

  private AuthenticatedChannel createAuthenticatedChannel(PlainChannel ch) throws Exception {
    Mac mac = new ScCbcMacPrepending(new BcAES());

    /// You could generate the key here and then somehow send it to the
    /// other party so the other party uses the same secret key
    // SecretKey macKey = SecretKeyGeneratorUtil.generateKey("AES");
    // Instead, we use a secretKey that has already been agreed upon by both
    /// parties:
    byte[] aesFixedKey =
        new byte[]{-61, -19, 106, -97, 106, 40, 52, -64, -115, -19, -87, -67, 98, 102, 16, 21};
    SecretKey key = new SecretKeySpec(aesFixedKey, "AES");
    mac.setKey(key);
    return new AuthenticatedChannel(ch, mac);
  }

  private PlainChannel getPlainSocketChannel(PartyData me, PartyData other) throws Exception {
    TwoPartyCommunicationSetup commSetup = new SocketCommunicationSetup(me, other);
    // Call the prepareForCommnication function to establish one connection
    // within 2000000 milliseconds.
    Map<String, Channel> connections = commSetup.prepareForCommunication(1, 2000000);
    return (PlainChannel) connections.values().toArray()[0];
  }


  private EncryptedChannel getSecureChannel(PlainChannel ch) throws Exception {
    byte[] aesFixedKey =
        new byte[]{-61, -19, 106, -97, 106, 40, 52, -64, -115, -19, -87, -67, 98, 102, 16, 21};
    SecretKey aesKey = new SecretKeySpec(aesFixedKey, "AES");
    AES encryptAes = new BcAES();
    encryptAes.setKey(aesKey);
    ScCTREncRandomIV enc = new ScCTREncRandomIV(encryptAes);
    AES macAes = new BcAES();
    macAes.setKey(aesKey);
    ScCbcMacPrepending cbcMac = new ScCbcMacPrepending(macAes);
    ScEncryptThenMac encThenMac = new ScEncryptThenMac(enc, cbcMac);
    EncryptedChannel secureChannel = new EncryptedChannel(ch, encThenMac);
    return secureChannel;
  }


  @Test
  public void testPlainSocketChannel() throws Exception {
    final byte[] data = new byte[]{-61, -19, 106, -9 - 67, 98, 102, 16, 21};
    final TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test =
        new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolImpl, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolImpl, ProtocolBuilderNumeric>() {
              @Override
              public void test() throws Exception {
                Party me = netConfs.get(conf.getMyId()).getMe();
                Party other = netConfs.get(conf.getMyId()).getParty(conf.getMyId() == 1 ? 2 : 1);
                PartyData meD =
                    new SocketPartyData(InetAddress.getByName(me.getHostname()), me.getPort());
                PartyData otherD = new SocketPartyData(InetAddress.getByName(other.getHostname()),
                    other.getPort());
                Channel channel = getPlainSocketChannel(meD, otherD);
                if (me.getPartyId() == 1) {
                  channel.send(data);
                } else {
                  byte[] received = (byte[]) channel.receive();
                  assertTrue(Arrays.equals(data, received));
                }
              }
            };
          }
        };
    runTest(test, 2);
  }


  @Test
  public void testAuthenticatedSocketChannel() throws Exception {
    final byte[] data = new byte[]{-61, -19, 106, -9 - 67, 98, 102, 16, 21};
    final TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test =
        new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolImpl, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolImpl, ProtocolBuilderNumeric>() {
              @Override
              public void test() throws Exception {
                Party me = netConfs.get(conf.getMyId()).getMe();
                Party other = netConfs.get(conf.getMyId()).getParty(conf.getMyId() == 1 ? 2 : 1);
                PartyData meD =
                    new SocketPartyData(InetAddress.getByName(me.getHostname()), me.getPort());
                PartyData otherD = new SocketPartyData(InetAddress.getByName(other.getHostname()),
                    other.getPort());
                PlainChannel channel = getPlainSocketChannel(meD, otherD);
                Channel res = createAuthenticatedChannel(channel);
                if (me.getPartyId() == 1) {
                  res.send(data);
                } else {
                  byte[] received = (byte[]) res.receive();
                  assertTrue(Arrays.equals(data, received));
                }
              }
            };
          }
        };
    runTest(test, 2);
  }


  @Test
  public void testSecureSocketChannel() throws Exception {
    final byte[] data = new byte[]{-61, -19, 106, -9 - 67, 98, 102, 16, 21};
    final TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric> test =
        new TestThreadFactory<ResourcePoolImpl, ProtocolBuilderNumeric>() {
          @Override
          public TestThread<ResourcePoolImpl, ProtocolBuilderNumeric> next() {
            return new TestThread<ResourcePoolImpl, ProtocolBuilderNumeric>() {
              @Override
              public void test() throws Exception {
                Party me = netConfs.get(conf.getMyId()).getMe();
                Party other = netConfs.get(conf.getMyId()).getParty(conf.getMyId() == 1 ? 2 : 1);
                PartyData meD =
                    new SocketPartyData(InetAddress.getByName(me.getHostname()), me.getPort());
                PartyData otherD = new SocketPartyData(InetAddress.getByName(other.getHostname()),
                    other.getPort());
                PlainChannel channel = getPlainSocketChannel(meD, otherD);
                Channel res = getSecureChannel(channel);
                if (me.getPartyId() == 1) {
                  res.send(data);
                } else {
                  byte[] received = (byte[]) res.receive();
                  assertTrue(Arrays.equals(data, received));
                }
              }
            };
          }
        };
    runTest(test, 2);
  }

}
