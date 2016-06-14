package dk.alexandra.fresco.suite.tinytables.util;

import java.io.IOException;
import java.io.Serializable;

import dk.alexandra.fresco.framework.network.SCENetwork;
import edu.biu.scapi.comm.Channel;

public class MyChannel implements Channel {

	private SCENetwork network;
	private int myId;

	public MyChannel(SCENetwork network, int myId) {
		this.network = network;
		this.myId = myId;
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Serializable receive() throws ClassNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void send(Serializable message) throws IOException {
		network.send(getOtherId(), message);
	}
	
	private int getOtherId() {
		return myId == 1 ? 2 : 1;
	}

}
