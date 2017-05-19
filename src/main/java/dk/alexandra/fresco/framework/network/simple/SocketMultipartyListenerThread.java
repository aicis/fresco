package dk.alexandra.fresco.framework.network.simple;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.comm.twoPartyComm.SocketPartyData;
import edu.biu.scapi.generals.Logging;

/**
 * This class listens to incoming connection to the current party. <p>
 * When an incoming connection is received, the listener checks which party is calling and set the received socket to this party.
 *
 *
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 *
 */
class SocketMultipartyListenerThread extends Thread{

    List<PartyData> parties;											//List of parties that should connect.
    protected Map<SocketPartyData, PlainTCPSocketChannel[]> channels;	//All channels between me and the other party. The received socket of each channel should be set when accepted.

    protected boolean bStopped = false;									//A flag that indicates if to keep on listening or stop.
    protected ServerSocket listener;									//Channel to listen on.

    SocketMultipartyListenerThread(){}

    /**
     * A constructor that open the server socket.
     * @param channelsPerParty The channels that should be set with receive socket for each other party in the protocol.
     * @param me The data of the current application.
     */
    SocketMultipartyListenerThread(Map<SocketPartyData, PlainTCPSocketChannel[]> channelsPerParty, SocketPartyData me) {

        doConstruct(channelsPerParty, me);
    }

    /**
     * sets the given channels and creates the server socket.
     * @param channelsPerParty The channels that should be set with receive socket for each other party in the protocol.
     * @param me The data of the current application.
     */
    protected void doConstruct(Map<SocketPartyData, PlainTCPSocketChannel[]> channelsPerParty, SocketPartyData me) {

        this.channels = channelsPerParty;
        createServerSocket(me);
    }


    /**
     * Created the {@link ServerSocketChannel}. <p>
     * We use the ServerSocketChannel rather than the regular ServerSocket since we want the accept to be non-blocking.
     *  If the accept function is blocking the flag bStopped will not be checked until the thread is unblocked.
     * @param me The data of the current application.
     */
    protected void createServerSocket(SocketPartyData me) {
        Logging.getLogger().info("Create server socket");
        //Open the ServerSocket using the ip and port in the given SocketPartyData object.
        try {
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.configureBlocking (false);
            listener = channel.socket();
            listener.bind (new InetSocketAddress (((SocketPartyData) me).getIpAddress(), ((SocketPartyData) me).getPort()));
        } catch (IOException e) {

            Logging.getLogger().log(Level.WARNING, e.toString());

        }
    }


    /**
     * Sets the flag bStopped to false. In the run function of this thread this flag is checked -
     * if the flag is true the run functions returns, otherwise continues.
     */
    void stopConnecting(){
        //Set the flag to true.
        bStopped = true;
    }



    /**
     * This function is the main function of the SocketMultipartyListenerThread. Mainly, we listen and accept valid connections
     * as long as the flag bStopped is false or until we have got as much connections as we should.<p>
     */
    public void run() {
        Logging.getLogger().info("run");
        //Prepare a map to hold the number of connected channels for each party.
        Map<PartyData, Integer> partiesChannelsCount = new HashMap<PartyData, Integer>();

        //Set the state of all channels to connecting.
        int count = setConnectingState(partiesChannelsCount);
        int i=0;

        //Loop for listening to incoming connections and make sure that this thread should not stopped.
        while (i < count && !bStopped) {

            SocketChannel socketChannel = null;
            try {

                Logging.getLogger().log(Level.INFO, "Trying to listen "+ listener.getLocalPort());

                //Use the server socket to listen to incoming connections.
                socketChannel = listener.getChannel().accept();

            }	catch (ClosedChannelException e) {
                Logging.getLogger().log(Level.WARNING, e.toString());
            } 	catch (IOException e) {

                Logging.getLogger().log(Level.WARNING, e.toString());
            }

            //If there was no connection request wait a second and try again.
            if(socketChannel==null){
                try {
                    Thread.sleep (1000);
                } catch (InterruptedException e) {

                    Logging.getLogger().log(Level.INFO, e.toString());
                }
                //If there was an incoming request, check that it valid and set the accepted socket to the right channel.
            } else{
                Socket socket = socketChannel.socket();
                i = setSocket(partiesChannelsCount, i, socket);
            }
        }

        Logging.getLogger().log(Level.INFO, "End of listening thread run");

        //After accepting all connections, close the thread.
        try {
            listener.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * After receiving a socket, check that it is valid and belongs to one of the expected parties.
     * If not, close the socket. otherwise, set it to the right party.
     * @param partiesChannelsCount A map that hold the number of connected channels for each party.
     * @param i Number of connected sockets of all parties.
     * @param socket The received socket.
     * @return The number of connected sockets of all parties.
     */
    protected int setSocket(Map<PartyData, Integer> partiesChannelsCount, int i, Socket socket) {
        Logging.getLogger().info("Got socket:" + socket);
        //Get the port of the calling party in order to determine which party is calling.
        SocketPartyData acceptedParty = createIncomingParty(socket);

        //If the accepted party is not a valid party close the connection. I.e. it is not in the parties list of the channels.
        if(!channels.containsKey(acceptedParty)){//an unauthorized party tried to connect
            //Close the socket.
            try {
                socket.close();
            } catch (IOException e) {

                Logging.getLogger().log(Level.WARNING, e.toString());
            }
            //If the accepted party is a valid party, set the received socket as the receive socket of the channel.
            //The send socket is set in the SocketMultipartyCommunicationSetup.connect() function.
        } else{
            PlainTCPSocketChannel[] partyChannels = channels.get(acceptedParty);
            int index = partiesChannelsCount.get(acceptedParty);
            partyChannels[index].setReceiveSocket(socket);
            partiesChannelsCount.put(acceptedParty, index + 1);

            //Increment the index of incoming connections.
            i++;
        }
        return i;
    }

    /**
     * Receives the port of the calling party in order to determine which party is calling.
     * @param socket The received socket.
     * @return The data of the calling party.
     */
    protected SocketPartyData createIncomingParty(Socket socket) {
        Logging.getLogger().info("create Incoming party");
        //Get the ip of the client socket.
        InetAddress inetAddr = socket.getInetAddress();
        //Receive the port of the client socket.
        byte[] output = new byte[4];
        try {
            socket.getInputStream().read(output);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        int port = ByteBuffer.wrap(output).getInt();

        //Create and returns a SocketPartyData object with the received ip and port.
        SocketPartyData acceptedParty = new SocketPartyData(inetAddr, port);
        return acceptedParty;
    }

    /**
     * Sets the state of all channels to CONNECTING and Initialize the partiesChannelsCount map.
     * @param partiesChannelsCount A map that hold the number of connected channels for each party.
     * @return The number of channels that should be connected.
     */
    protected int setConnectingState(Map<PartyData, Integer> partiesChannelsCount) {
        Logging.getLogger().info("Set connecting state");
        //Create an iterator that go over the parties list.
        Iterator<SocketPartyData> parties = channels.keySet().iterator();
        int count = 0;
        //Get the channels of each party, and set every one of them the CONNECTING state.
        while (parties.hasNext()){
            PartyData key = parties.next();
            PlainTCPSocketChannel[] partyChannels = channels.get(key);
            int size = partyChannels.length;
            for (int i=0; i<size; i++){

                partyChannels[i].setState(PlainTCPSocketChannel.State.CONNECTING);
                count++;
            }
            //Initialize the connected channels map with the party data and 0 connected channels.
            partiesChannelsCount.put(key, 0);
        }
        return count;
    }
}