package dk.alexandra.fresco.framework.network.simple;

import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.comm.twoPartyComm.SocketPartyData;
import edu.biu.scapi.generals.Logging;

import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

/**
 * This class manage the socket channels in both two party and multiparty communications. <P>
 * The class does the creation of the channels and the connect step during the communication setup. <p>
 * <p>
 * Although it is declared "public" it is not for public usage and we recommend not to use it.
 * In order to setup a communication use one of the communication setup classes.
 *
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 */
public class TwoPartySocketConnector {

    private SocketPartyData me;                        //The data of the current application
    private SocketPartyData other;                    //The data of the other application to communicate with.
    private boolean bStopped = false;                //A flag that indicates if to keep on listening or stop.
    private boolean isSecure;                        // A flag that indicates to use SSL or not.
    private SSLSocketFactory factory;                //In case of SSL communication, the sockets are created via this factory.
    private Map<String, PlainTCPSocketChannel> connectionsMap;


    /**
     * A constructor that set the parties.<p>
     * In case this constructor has been called, the created channels will be plain.
     *
     * @param me    The data of the current application
     * @param party The data of the other application to communicate with.
     */
    public TwoPartySocketConnector(PartyData me, PartyData party) {
        this.me = (SocketPartyData) me;
        this.other = (SocketPartyData) party;
        this.isSecure = false;
    }

    /**
     * A constructor that set the parties and the factory.<p>
     * In case this constructor has been called, the created channels will be secure.
     *
     * @param me      The data of the current application
     * @param party   The data of the other application to communicate with.
     * @param factory used to create the sockets.
     */
    public TwoPartySocketConnector(PartyData me, PartyData party, SSLSocketFactory factory) {
        this(me, party);
        this.isSecure = true;
        this.factory = factory;
    }

    /**
     * Creates the channels and give them the names in connectionsIds array.
     *
     * @param connectionsIds Array of channels names.
     * @param checkIdentity  A flag that indicates whether or not to check that incoming connection is from the expected party.
     * @return PlainTCPSocketChannel[] Array of created channels.
     */
    public PlainTCPSocketChannel[] createChannels(String[] connectionsIds, boolean checkIdentity) {
        //Initiate the channels map.
        connectionsMap = new HashMap<String, PlainTCPSocketChannel>();

        //Create an InetSocketAddress of the other party.
        InetSocketAddress inetSocketAdd = new InetSocketAddress(other.getIpAddress(), other.getPort());

        int size = connectionsIds.length;
        //Create an array to hold the created channels.
        PlainTCPSocketChannel[] channels = new PlainTCPSocketChannel[size];

        //Create the number of channels as requested, give them the names in connectionsIds and set them in the establishedConnections object.
        for (int i = 0; i < size; i++) {
            channels[i] = new PlainTCPSocketChannel(inetSocketAdd, checkIdentity, me);
            //Set to NOT_INIT state.
            channels[i].setState(PlainTCPSocketChannel.State.NOT_INIT);
            // Add the plainTCPSocketChannel to the map.
            connectionsMap.put(connectionsIds[i], channels[i]);
        }

        return channels;
    }


    /**
     * This function calls each plainTCPSocketChannel to connect to the other party.
     *
     * @param channels between me to the other party.
     */
    public void connect(PlainTCPSocketChannel[] channels) {

        //For each plainTCPSocketChannel, call the connect function until the plainTCPSocketChannel is actually connected.
        for (int i = 0; i < channels.length && !bStopped; i++) {

            //while connection has not been stopped by owner and connection has failed.
            while (!channels[i].isSendConnected() && !bStopped) {

                //Set the state to connecting.
                channels[i].setState(PlainTCPSocketChannel.State.CONNECTING);
                Logging.getLogger().log(Level.INFO, "state: connecting " + channels[i].toString());

                //Try to connect.
                channels[i].connect();

            }

            Logging.getLogger().log(Level.INFO, "End of securing thread run" + channels[i].toString());
        }
    }

    /**
     * Sets the flag bStopped to false. In the run function of this thread this flag is checked -
     * if the flag is true the run functions returns, otherwise continues.
     */
    public void stopConnecting() {

        //Set the flag to true.
        bStopped = true;

        PlainTCPSocketChannel plainTCPSocketChannel;
        String id;

        //Set an iterator for the connection map.
        Iterator<String> iterator = connectionsMap.keySet().iterator();

        //Go over the map and close all connection.
        while (iterator.hasNext()) {
            //Get the plainTCPSocketChannel.
            id = iterator.next();
            plainTCPSocketChannel = connectionsMap.get(id);

            //Close the plainTCPSocketChannel.
            plainTCPSocketChannel.close();
        }

        //Remove all channels from the map.
        connectionsMap.clear();
    }

    /**
     * Returns the object that holds the connections.
     *
     * @return EstablishedSocketConnections
     */
    public Map<String, PlainTCPSocketChannel> getConnections() {
        return connectionsMap;
    }

    /**
     * This function serves as a barrier. It is called from the prepareForCommunication function. The idea
     * is to let all the threads finish running before proceeding.
     */
    public void verifyConnectingStatus() {

        //Wait until the thread has been stopped or all the channels are connected.
        while (!bStopped && !areAllConnected()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

                Logging.getLogger().log(Level.FINEST, e.toString());
            }
        }
    }

    /**
     * @return true if all the channels are in READY state, false otherwise.
     */
    private boolean areAllConnected() {
        //Set an iterator for the connection map.
        Collection<PlainTCPSocketChannel> c = connectionsMap.values();
        Iterator<PlainTCPSocketChannel> itr = c.iterator();

        PlainTCPSocketChannel plainChannel;
        //Go over the map and check if all the connections are in READY state.
        while (itr.hasNext()) {
            plainChannel = (PlainTCPSocketChannel) itr.next();
            if (plainChannel.getState() != PlainTCPSocketChannel.State.READY) {
                return false;
            }
        }

        return true;
    }

    /**
     * Enables Nagle's algorithm.
     */
    public void enableNagle() {
        PlainTCPSocketChannel plainTCPChannel;
        PlainTCPSocketChannel plainTCPSocketChannel;
        String id;

        //Set an iterator for the connection map.
        Iterator<String> iterator = connectionsMap.keySet().iterator();

        //Go over the map and enable/disable each plainTCPSocketChannel with the Nagle algorithm.
        while (iterator.hasNext()) {

            //Get the plainTCPSocketChannel.
            id = iterator.next();
            plainTCPSocketChannel = connectionsMap.get(id);

            //Check if the plainTCPSocketChannel is a plain tcp plainTCPSocketChannel. Otherwise there is no point for the Nagle algorithm.
            if (plainTCPSocketChannel instanceof PlainTCPSocketChannel) {
                plainTCPChannel = (PlainTCPSocketChannel) plainTCPSocketChannel;

                //Enable nagle.
                //EBO: TCP No Delay
                plainTCPChannel.enableNage(false);
            }
        }

    }

    /**
     * @return the number of created channels.
     */
    public int getConnectionsCount() {
        return connectionsMap.size();
    }

    /**
     * Returned this object to a fresh state by removing all connections from the map and setting bStopped to false.
     */
    public void reset() {

        bStopped = false;

    }

}