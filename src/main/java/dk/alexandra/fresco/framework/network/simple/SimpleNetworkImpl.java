package dk.alexandra.fresco.framework.network.simple;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.network.Network;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.comm.twoPartyComm.SocketPartyData;
import edu.biu.scapi.generals.Logging;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class SimpleNetworkImpl implements Network {
    private int defaultPlainTCPSocketChannel = 0;
    private int PlainTCPSocketChannelAmount;
    private NetworkConfiguration conf;
    private LinkedList<PartyData> parties;
    private HashMap<Integer, PartyData> idToPartyData;
    private HashMap<Integer, BlockingQueue<Serializable>> queues;
    private Map<PartyData, Map<String, PlainTCPSocketChannel>> connections;

    @Override
    public void init(NetworkConfiguration conf, int PlainTCPSocketChannelAmount) {
        this.PlainTCPSocketChannelAmount = PlainTCPSocketChannelAmount;
        this.conf = conf;
    }

    @Override
    public void connect(int timeoutMillis) throws IOException {

        // Convert FRESCO configuration to SCAPI configuration.
        parties = new LinkedList<PartyData>();
        idToPartyData = new HashMap<Integer, PartyData>();
        System.out.println(conf);
        for (int id = 1; id <= conf.noOfParties(); id++) {
            Party frescoParty = conf.getParty(id);
            String iadrStr = frescoParty.getHostname();
            InetAddress iadr = InetAddress.getByName(iadrStr);
            int port = frescoParty.getPort();
            SocketPartyData scapyParty = new SocketPartyData(iadr, port);
            parties.add(scapyParty);
            idToPartyData.put(id, scapyParty);
        }
        // SCAPI requires party itself to be first in list.
        Collections.swap(parties, 0, conf.getMyId() - 1);

        List<PartyData> others = new LinkedList<PartyData>(parties);
        others.remove(0);
        // Create the communication setup class.
        SocketMultipartyCommunicationSetup commSetup = new SocketMultipartyCommunicationSetup(parties);
        // Request one PlainTCPSocketChannel between me and each other party.
        HashMap<PartyData, Object> connectionsPerParty = new HashMap<PartyData, Object>(
                others.size());
        //queue to self
        this.queues = new HashMap<Integer, BlockingQueue<Serializable>>();
        for (int i = 0; i < others.size(); i++) {
            connectionsPerParty.put(others.get(i), this.PlainTCPSocketChannelAmount);
        }

        for (int i = 0; i < this.PlainTCPSocketChannelAmount; i++) {
            this.queues.put(i, new ArrayBlockingQueue<Serializable>(10000));
        }

        try {
            connections = commSetup.prepareForCommunication(connectionsPerParty, timeoutMillis);
        } catch (TimeoutException e) {
            throw new IOException(e);
        }
    }

    /**
     * Close all PlainTCPSocketChannels to other parties.
     */
    public void close() throws IOException {
        if (connections != null) {
            for (Map<String, PlainTCPSocketChannel> m : connections.values()) {
                for (PlainTCPSocketChannel c : m.values()) {
                    c.close();
                }
            }
        }
    }

    /**
     * Send using default PlainTCPSocketChannel (0).
     * <p>
     * TODO: When writing to TCP socket, does message always (1) get send eventually, or (2) does it
     * risk getting buffered indefinitely (until explicitly calling flush/close)?
     * <p>
     * TODO: There is also potential deadlock with TCP if both parties send large buffers to
     * each other simultaneously.
     *
     * @param receiverId Non-negative id of player to receive data.
     * @param data
     * @throws IOException
     */
    public void send(int receiverId, byte[] data) throws IOException {
//        Logging.getLogger().info("Send to:" + receiverId + "data.length=" + data.length);
        send(defaultPlainTCPSocketChannel, receiverId, data);
    }


    /**
     * Receive data using default PlainTCPSocketChannel (0).
     *
     * @param id Non-negative id of player from which to receive data.
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public byte[] receive(int id) throws IOException {
        return receive(defaultPlainTCPSocketChannel, id);
    }

    public void send(int PlainTCPSocketChannel, Map<Integer, byte[]> output) throws IOException {
        for (int playerId : output.keySet()) {
            this.send(PlainTCPSocketChannel, playerId, output.get(playerId));
        }
    }

    public Map<Integer, Serializable> receive(int PlainTCPSocketChannel, Set<Integer> expectedInputForNextRound) throws IOException {
        //TODO: Maybe use threading for each player
        Map<Integer, Serializable> res = new HashMap<Integer, Serializable>();
        for (int i : expectedInputForNextRound) {
            byte[] r = this.receive(PlainTCPSocketChannel, i);
            res.put(i, r);
        }
        return res;
    }

    public int getMyId() {
        return this.conf.getMyId();
    }

    public int getNoParties() {
        return this.conf.noOfParties();
    }

    @Override
    public void send(int PlainTCPSocketChannel, int partyId, byte[] data)
            throws IOException {
        if (partyId == this.conf.getMyId()) {
            this.queues.get(PlainTCPSocketChannel).add(data);
            return;
        }
        if (!idToPartyData.containsKey(partyId)) {
            throw new MPCException("No party with id " + partyId);
        }
        PartyData receiver = idToPartyData.get(partyId);
        Map<String, PlainTCPSocketChannel> PlainTCPSocketChannels = connections.get(receiver);
        PlainTCPSocketChannel c = PlainTCPSocketChannels.get("" + PlainTCPSocketChannel);
        c.send(data);
    }

    @Override
    public byte[] receive(int PlainTCPSocketChannel, int partyId) throws IOException {
//        Logging.getLogger().info("Receive from:" + partyId + "(Me=" + this.conf.getMyId() + ")");
        if (partyId == this.conf.getMyId()) {
            byte[] res = (byte[]) this.queues.get(PlainTCPSocketChannel).poll();
            if (res == null) {
                throw new MPCException("Self(" + partyId + ") have not send anything on PlainTCPSocketChannel " + PlainTCPSocketChannel + "before receive was called.");
            }
            return res;
        } else {
            PartyData receiver = idToPartyData.get(partyId);
            Map<String, PlainTCPSocketChannel> PlainTCPSocketChannels = connections.get(receiver);
            PlainTCPSocketChannel c = PlainTCPSocketChannels.get("" + PlainTCPSocketChannel);
            if (c == null) {
                throw new MPCException(
                        "Trying to send via PlainTCPSocketChannel " + PlainTCPSocketChannel + ", but this network was initiated with only " + this.PlainTCPSocketChannelAmount + " PlainTCPSocketChannels.");
            }
            byte[] res = null;
            try {
                res = (byte[]) c.receive();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Weird class not found exception, sry. ", e);
            }
            return res;
        }
    }
}
