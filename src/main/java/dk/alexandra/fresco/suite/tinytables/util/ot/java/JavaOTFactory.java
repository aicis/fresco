package dk.alexandra.fresco.suite.tinytables.util.ot.java;

import java.security.SecureRandom;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTFactory;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;

public class JavaOTFactory implements OTFactory {

	private Network network;
	private int myId;
	private SecureRandom random;

	public JavaOTFactory(Network network, int myId, SecureRandom random) {
		this.network = network;
		this.myId = myId;
		this.random = random;
	}
	
	@Override
	public OTSender createOTSender() {
		return new JavaOTSender(network, myId, random);
	}

	@Override
	public OTReceiver createOTReceiver() {
		return new JavaOTReceiver(network, myId, random);
	}

}
