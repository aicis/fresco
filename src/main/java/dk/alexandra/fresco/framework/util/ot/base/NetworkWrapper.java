package dk.alexandra.fresco.framework.util.ot.base;

import java.io.IOException;
import java.io.Serializable;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.tinytables.util.Util;
import edu.biu.scapi.comm.Channel;

/**
 * This class wraps an instance of {@link Network} (in the FRESCO sense) as an
 * instance of {@link Channel} (as in SCAPI).
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class NetworkWrapper implements Channel {

	private Network network;
	private int myId;

	public NetworkWrapper(Network network, int myId) {
		this.network = network;
		this.myId = myId;
	}

	@Override
	public void close() {

	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public Serializable receive() throws ClassNotFoundException, IOException {
		return network.receive("0", Util.otherPlayerId(myId));
	}

	@Override
	public void send(Serializable otInputs) throws IOException {
		network.send("0", Util.otherPlayerId(myId), otInputs);
	}

}
