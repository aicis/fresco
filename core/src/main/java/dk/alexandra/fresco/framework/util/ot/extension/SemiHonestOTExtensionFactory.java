package dk.alexandra.fresco.framework.util.ot.extension;

import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ot.OTFactory;
import dk.alexandra.fresco.framework.util.ot.OTReceiver;
import dk.alexandra.fresco.framework.util.ot.OTSender;

public class SemiHonestOTExtensionFactory implements OTFactory {

	private Network network;
	private int myId;
	private int securityParameter;
	private OTFactory baseOT;
	private Random random;

	public SemiHonestOTExtensionFactory(Network network, int myId,
			int securityParameter, OTFactory baseOT, Random random) {
		this.network = network;
		this.myId = myId;
		this.securityParameter = securityParameter;
		this.baseOT = baseOT;
		this.random = random;
	}
	
	@Override
	public OTSender createOTSender() {
		return new SemiHonestOTExtensionSender(network, myId, securityParameter, baseOT, random);
	}

	@Override
	public OTReceiver createOTReceiver() {
		return new SemiHonestOTExtensionReceiver(network, myId, securityParameter, baseOT, random);
	}

}
