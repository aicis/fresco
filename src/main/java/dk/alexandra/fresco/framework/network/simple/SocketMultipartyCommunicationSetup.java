package dk.alexandra.fresco.framework.network.simple;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.apache.commons.exec.TimeoutObserver;
import org.apache.commons.exec.Watchdog;

import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.comm.twoPartyComm.SocketPartyData;
import edu.biu.scapi.generals.Logging;

/**
 * This class implements a communication between multiple parties using TCP sockets.<p>
 * Each created channel contains two sockets; one is used to send messages and one to receive messages.<p>
 * This class encapsulates the stage of connecting to other parties. In actuality, the connection to other parties is
 * performed in a few steps, which are not visible to the outside user.
 * These steps are:<p>
 * <ul>
 * <li>For each party,</li>
 * <ul>
 * <li>For each requested channel, create an actual TCP socket with the other party. This socket is used to send messages</li>
 * </ul>
 * <li>Create a server socket that listen to all other parties calls. When a call is received, check who is the connecting party and use the created socket to receive messages from this party.</li>
 * <li>run a protocol that checks if all the necessary connections were set between my party and other parties.</li>
 * <li>In the end return to the calling application a set of connected and ready channels to be used throughout a cryptographic protocol.</li>
 * </ul>
 * From this point onwards, the application can send and receive messages in each connection as required by the protocol.<p>
 *
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 *
 */
public class SocketMultipartyCommunicationSetup implements TimeoutObserver{

    protected boolean bTimedOut = false;							//Indicated whether or not to end the communication.
    private boolean enableNagle = false;							//Indicated whether or not to use Nagle optimization algorithm.
    protected Map<PartyData, TwoPartySocketConnector> connectors;	//Used to create and connect the channels to the other parties.
    private Watchdog watchdog;										//Used to measure times.
    private Map<PartyData, Integer> connectionsNumber;				//Holds the number of created connections for each party.
    private SocketMultipartyListenerThread listeningThread ;		//Listen to calls from the other parties.
    protected SocketPartyData me;									//The data of the current application.

    protected SocketMultipartyCommunicationSetup(){}

    /**
     * A constructor that set the given list of parties.
     * @param parties List of parties to communicate with.
     */
    public SocketMultipartyCommunicationSetup(List<PartyData> parties){

        doConstruct(parties);

    }

    /**
     * Constructs this communicationSetup object.<p>
     * Sets the parties and creates a conenctor object between this party and any other party.
     * @param parties List of parties to communicate with.
     */
    protected void doConstruct(List<PartyData> parties) {
        connectionsNumber = new HashMap<PartyData, Integer>();
        connectors = new HashMap<PartyData, TwoPartySocketConnector>();

        //All parties should be instances of SocketPartyData. IN any other case, throw IllegalArgumentException.
        if (!(parties.get(0) instanceof SocketPartyData)){
            throw new IllegalArgumentException("all parties should be instances of SocketPartyData");
        } else{
            //Set the current party.
            me = (SocketPartyData) parties.get(0);
        }

        int size = parties.size();
        //Create a connector between me and any other party in the list and put it in the connectors map.
        for (int i=1; i<size; i++){
            PartyData data = parties.get(i);
            if (!(data instanceof SocketPartyData)){
                throw new IllegalArgumentException("all parties should be instances of SocketPartyData");
            } else{
                connectionsNumber.put(data, 0);
                TwoPartySocketConnector connector = createConnector(parties.get(i));
                connectors.put(data, connector);
            }
        }
    }

    /**
     * Create a connector between me and the given party.
     * The created connector will create a plain channels, as needed in this communication setup class.
     * @param party The other party to communicate with.
     * @return the created connector object.
     */
    protected TwoPartySocketConnector createConnector(PartyData party) {
        TwoPartySocketConnector connector = new TwoPartySocketConnector(me, party);
        return connector;
    }

    public Map<PartyData, Map<String, PlainTCPSocketChannel>> prepareForCommunication(Map<PartyData, Object> connectionsPerParty, long timeOut)
            throws TimeoutException {

        //Start the watch dog with timeout
        watchdog = new Watchdog(timeOut);
        //Add this instance as the observer in order to receive the event of time out.
        watchdog.addTimeoutObserver(this);
        //Start the clock.
        watchdog.start();

        //Establish all connections between all parties.
        establishConnections(connectionsPerParty);

        //Verify that all connections have been connected. This function will block until all connections will be established.
        verifyConnectingStatus(connectionsPerParty.keySet().iterator());

        //If we already know that all the connections were established we can stop the watchdog.
        watchdog.stop();

        //IN case of time out, throw a TimeoutException.
        if (bTimedOut){
            throw new TimeoutException("timeout has occurred");
        }

        //Create the map to contains all channels between all parties.
        Map<PartyData, Map<String, PlainTCPSocketChannel>> returnedChannels = new HashMap<>();

        Iterator<PartyData> parties = connectionsPerParty.keySet().iterator();
        //Get the channels between me and every other party and put them in the returnedChannels map.
        while (parties.hasNext()){
            //The party data is the key to the map.
            PartyData key = parties.next();

            //Get the connector between me and the other party.
            TwoPartySocketConnector connector = connectors.get(key);

            //Set Nagle algorithm if needed.
            if (enableNagle){
                connector.enableNagle();
            }

            //Update the number of the created connections for this party.
            connectionsNumber.put(key, connectionsNumber.get(key) + connector.getConnectionsCount());

            //Add the channels of this connector to the channels map.
            returnedChannels.put(key, connector.getConnections());

            //Reset the connector in order to be able to reuse it next time this function will be called.
            connector.reset();
        }

        //Return the map of channels held in the established connection object.
        return returnedChannels;
    }

    /**
     * This function checks that all the channels have been established.
     * This function serves as a barrier. It is called from the prepareForCommunication function. The idea
     * is to let all the threads finish running before proceeding.
     *
     * @param connectObjects Contained the data of the parties to connect to.
     */
    private void verifyConnectingStatus(Iterator<PartyData> connectObjects) {
        //call the verifyConnectingStatus function of each connector.
        //Each verifyConnectingStatus function will returned only when all the channels between me and the other party are established.
        while (connectObjects.hasNext()){
            TwoPartySocketConnector connector = connectors.get(connectObjects.next());
            connector.verifyConnectingStatus();
        }
    }

    /**
     * This function does the actual creation of the communication between the parties.<p>
     * A connected channel between two parties has two sockets. One is used by P1 to send messages and p2 receives them,
     * while the other used by P2 to send messages and P1 receives them.
     *
     * The function does the following steps:
     * 1. Creates all channels between me and the other parties.
     * 2. Start a listening thread that accepts calls from the other parties.
     * 3. Create a connection thread for each other party that does the actual connecting.
     * The thread uses the connector object that calls each channel's connect function in order to connect each channel to the other party.
     * @param connectionsPerParty The names of the requested connections for each other party.
     *
     */
    protected void establishConnections(Map<PartyData, Object> connectionsPerParty){

        Map<SocketPartyData, PlainTCPSocketChannel[]> channelsPerParty = new HashMap<SocketPartyData, PlainTCPSocketChannel[]>();

        if (!bTimedOut){
            //Creates all channels between me and the other parties.
            createChannels(connectionsPerParty, channelsPerParty);

            //Create a listening thread with the created channels.
            //The listening thread receives calls from the other parties and set the created sockets as the receiveSocket of the channels.
            listeningThread = createListener(channelsPerParty);
            listeningThread.start();

            //Create a connection thread for each other party that does the actual connecting.
            Iterator<PartyData> connectParties = connectionsPerParty.keySet().iterator();
            while (connectParties.hasNext()){
                PartyData key = connectParties.next();
                ConnectingThread thread = new ConnectingThread(channelsPerParty.get(key), connectors.get(key));
                //Start the thread. It uses the connector object that calls each channel's connect function in order to connect each channel to the other party.
                thread.start();
            }
        }
    }

    /**
     * Creates a listener that listens for incoming calls from plain sockets.
     * @param channelsPerParty The created channels between me and each other party.
     * @return the created listener.
     */
    protected SocketMultipartyListenerThread createListener(Map<SocketPartyData, PlainTCPSocketChannel[]> channelsPerParty) {
        return new SocketMultipartyListenerThread(channelsPerParty, me);
    }

    /**
     * Creates all channels between me and the other parties.
     * @param connectionsPerParty
     * @param channelsPerParty
     */
    private void createChannels(Map<PartyData, Object> connectionsPerParty, Map<SocketPartyData, PlainTCPSocketChannel[]> channelsPerParty) {
        Iterator<PartyData> parties = connectionsPerParty.keySet().iterator();

        //Get the connector between me and each other party, and use it to create the requested channels.
        while (parties.hasNext()){
            SocketPartyData key = (SocketPartyData) parties.next(); //Get the other party data.
            TwoPartySocketConnector connector = connectors.get(key); //Get the connector between me and the party.

            Object reqChannels = connectionsPerParty.get(key);
            String[] names;

            //In case the user gave the number of requested channels, create their names.
            if (reqChannels instanceof Integer){
                int connectionsNum = (Integer) reqChannels;
                names = new String[connectionsNum];

                for (int i=0; i<connectionsNum; i++){
                    int bigestConnection = connectionsNumber.get(key);
                    names[i] = Integer.toString(bigestConnection);
                    connectionsNumber.put(key, bigestConnection + 1);
                }
            } else{ //else, the user gave the names of the requested channels, set them.
                names = (String[]) connectionsPerParty.get(key);
            }

            //Call the connector to create the channels.
            PlainTCPSocketChannel[] channels = connector.createChannels(names, true);
            //Put the channels in the map.
            channelsPerParty.put(key, channels);
        }
    }

    /**
     * This function is called by the infrastructure of the Watchdog if the previously set timeout has passed. (Do not call this function).
     */
    public void timeoutOccured(Watchdog w) {

        Logging.getLogger().log(Level.INFO, "Timeout occured");

        //Timeout has passed, set the flag.
        bTimedOut = true;

        //Further stop the listening thread if it still runs. Similarly, it sets the flag of the listening thread to stopped.
        if(listeningThread != null)
            listeningThread.stopConnecting();

        //Further stop the connectors objects if they still run.
        if(connectors != null){
            Object[] connectorsobjects = connectors.values().toArray();
            for(int i=0; i<connectorsobjects.length; i++){
                ((TwoPartySocketConnector) connectorsobjects[i]).stopConnecting();
            }
        }
    }

    /**
     * This implementation has nothing to close besides the sockets (which are being closed by the channel instances).
     */
    public void close() {}

    /**
     * This thread has a connector object. The run function calls the connector.connect() function in order to connect the other party.
     *
     * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
     *
     */
    class ConnectingThread extends Thread{

        TwoPartySocketConnector connector;
        PlainTCPSocketChannel[] channels;

        /**
         * Sets the given connector and channels.
         * @param channels
         * @param connector
         */
        ConnectingThread(PlainTCPSocketChannel[] channels, TwoPartySocketConnector connector){
            this.channels = channels;
            this.connector = connector;
        }

        //Runs the connect function with the channels in order to connect to the other party.
        public void run(){
            connector.connect(channels);
        }
    }

}