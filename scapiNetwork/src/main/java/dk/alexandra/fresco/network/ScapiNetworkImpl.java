package dk.alexandra.fresco.network;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.EncryptedChannel;
import edu.biu.scapi.comm.PlainChannel;
import edu.biu.scapi.comm.multiPartyComm.MultipartyCommunicationSetup;
import edu.biu.scapi.comm.multiPartyComm.SocketMultipartyCommunicationSetup;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.comm.twoPartyComm.SocketPartyData;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.midLayer.symmetricCrypto.encryption.ScCTREncRandomIV;
import edu.biu.scapi.midLayer.symmetricCrypto.encryption.ScEncryptThenMac;
import edu.biu.scapi.midLayer.symmetricCrypto.mac.ScCbcMacPrepending;
import edu.biu.scapi.primitives.prf.AES;
import edu.biu.scapi.primitives.prf.bc.BcAES;
import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network based on SCAPI network layer.
 *
 * For now it only uses non-encrypted socket-based communication.
 */
public class ScapiNetworkImpl implements Network, Closeable {

  private NetworkConfiguration conf;
  private boolean connected = false;

  // Unless explicitly named, SCAPI channels are named with
  // strings "0", "1", etc.
  private Map<PartyData, Map<String, Channel>> connections;
  private Map<Integer, PartyData> idToPartyData;
  private int channelAmount;

  // Queue for self-sending
  private Map<Integer, BlockingQueue<Serializable>> queues;
  private static Logger logger = LoggerFactory.getLogger(ScapiNetworkImpl.class);

  public ScapiNetworkImpl() {
  }

  /**
   * @param conf - The configuration with info about whom to connect to.
   * @param channelAmount The amount of channels each player needs to each other.
   */
  public void init(NetworkConfiguration conf, int channelAmount) {
    this.channelAmount = channelAmount;
    this.conf = conf;
  }

  // TODO: Include player to integer map to indicate
  // how many channels are wanted to each player.
  // Implement this also for send to self queues.
  public void connect(int timeoutMillis) {
    if (connected) {
      return;
    }
    // Convert FRESCO configuration to SCAPI configuration.
    List<PartyData> parties = new LinkedList<>();
    idToPartyData = new HashMap<>();
    List<String> sharedSecretKeys = new LinkedList<>();
    logger.info("Connecting using: " + conf);
    for (int id = 1; id <= conf.noOfParties(); id++) {
      Party frescoParty = conf.getParty(id);
      String iadrStr = frescoParty.getHostname();
      InetAddress iadr;
      try {
        iadr = InetAddress.getByName(iadrStr);
      } catch (UnknownHostException e) {
        throw new RuntimeException("Cannot find party with adress=" + iadrStr);
      }
      int port = frescoParty.getPort();
      SocketPartyData scapyParty = new SocketPartyData(iadr, port);
      parties.add(scapyParty);
      sharedSecretKeys.add(frescoParty.getSecretSharedKey());
      idToPartyData.put(id, scapyParty);
    }
    // SCAPI requires party itself to be first in list.
    Collections.swap(parties, 0, conf.getMyId() - 1);
    Collections.swap(sharedSecretKeys, 0, conf.getMyId() - 1);

    List<PartyData> others = new LinkedList<>(parties);
    others.remove(0);
    // Create the communication setup class.
    MultipartyCommunicationSetup commSetup = new SocketMultipartyCommunicationSetup(parties);
    // Request one channel between me and each other party.
    HashMap<PartyData, Object> connectionsPerParty = new HashMap<>(others.size());
    // queue to self
    this.queues = new HashMap<>();
    for (PartyData other : others) {
      connectionsPerParty.put(other, this.channelAmount);
    }

    for (int i = 0; i < this.channelAmount; i++) {
      // TODO: figure out correct number for capacity.
      this.queues.put(i, new ArrayBlockingQueue<>(10000));
    }

    try {
      connections = commSetup.prepareForCommunication(connectionsPerParty, timeoutMillis);
    } catch (TimeoutException e) {
      throw new RuntimeException(e);
    }

    // Enable secure (auth + encrypted) channels if a key is specified.
    for (int id = 0; id < sharedSecretKeys.size(); id++) {
      int partyId = id + 1;
      if (this.conf.getMyId() != partyId && sharedSecretKeys.get(id) != null) {
        logger.info("Using authentication and encryption for channel(s) to party " + partyId);
        for (int i = 0; i < this.channelAmount; i++) {
          PartyData pd = idToPartyData.get(partyId);
          Map<String, Channel> channels = connections.get(pd);
          String cStr = "" + i;
          PlainChannel c = (PlainChannel) channels.get(cStr);
          Channel secureChannel;
          String sharedSecretKey = sharedSecretKeys.get(id);
          try {
            secureChannel = getSecureChannel(c, sharedSecretKey);
          } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid AES key (shared secret key): "
                + sharedSecretKey, e);
          } catch (SecurityLevelException e) {
            throw new RuntimeException("SCAPI security level exception "
                + "when creating channel " + cStr + " towards " + partyId, e);
          }

          channels.put(cStr, secureChannel);
        }
      }
    }
  }

  private EncryptedChannel getSecureChannel(PlainChannel ch, String base64EncodedSSKey)
      throws InvalidKeyException, SecurityLevelException {
    byte[] aesFixedKey = java.util.Base64.getDecoder().decode(base64EncodedSSKey);
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


  /**
   * Close all channels to other parties.
   */
  @Override
  public void close() throws IOException {
    if (connections != null) {
      for (Map<String, Channel> m : connections.values()) {
        for (Channel c : m.values()) {
          c.close();
        }
      }
    }
    connected = false;
  }

  public void send(int channel, Map<Integer, byte[]> output) throws IOException {
    for (int playerId : output.keySet()) {
      this.send(playerId, output.get(playerId));
    }
  }

  public Map<Integer, Serializable> receive(int channel, Set<Integer> expectedInputForNextRound)
      throws IOException {
    // TODO: Maybe use threading for each player
    Map<Integer, Serializable> res = new HashMap<>();
    for (int i : expectedInputForNextRound) {
      byte[] r = this.receive(i);
      res.put(i, r);
    }
    return res;
  }

  public int getMyId() {
    return this.conf.getMyId();
  }


  @Override
  public void send(int partyId, byte[] data) {
    if (partyId == this.conf.getMyId()) {
      this.queues.get(0).add(data);
      return;
    }
    if (!idToPartyData.containsKey(partyId)) {
      throw new IllegalArgumentException("No party with id " + partyId);
    }
    PartyData receiver = idToPartyData.get(partyId);
    Map<String, Channel> channels = connections.get(receiver);
    Channel c = channels.get("" + 0);
    try {
      c.send(data);
    } catch (IOException e) {
      throw new RuntimeException("Cannot send", e);
    }
  }

  @Override
  public byte[] receive(int partyId) {
    if (partyId == this.conf.getMyId()) {
      byte[] res = (byte[]) this.queues.get(0).poll();
      if (res == null) {
        throw new IllegalStateException(
            "Self(" + partyId + ") have not send anything on channel " + 0
                + "before receive was called.");
      }
      return res;
    } else {
      PartyData receiver = idToPartyData.get(partyId);
      Map<String, Channel> channels = connections.get(receiver);
      Channel c = channels.get("" + 0);
      if (c == null) {
        throw new IllegalStateException("Trying to send via channel " + 0
            + ", but this network was initiated with only " + this.channelAmount + " channels.");
      }
      byte[] res;
      try {
        res = (byte[]) c.receive();
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("Weird class not found exception, sry. ", e);
      } catch (IOException e) {
        throw new RuntimeException("Cannot receive", e);
      }
      return res;
    }
  }

  @Override
  public int getNoOfParties() {
    return conf.noOfParties();
  }

}
