package dk.alexandra.fresco.suite.tinytables.util.ot.java;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTFactory;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;

public class JavaOTFactory implements OTFactory {

	private Network network;
	private int myId;

	public JavaOTFactory(Network network, int myId) {
		this.network = network;
		this.myId = myId;
	}
	
	@Override
	public OTSender createOTSender() {
		return new JavaOTSender(network, myId);
	}

	@Override
	public OTReceiver createOTReceiver() {
		// TODO Auto-generated method stub
		return new JavaOTReceiver(network, myId);
	}

}
