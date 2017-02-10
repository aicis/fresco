package dk.alexandra.fresco.framework.util.ot.base;

import java.security.SecureRandom;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ot.OTFactory;
import dk.alexandra.fresco.framework.util.ot.OTReceiver;
import dk.alexandra.fresco.framework.util.ot.OTSender;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.semiHonest.OTSemiHonestDDHBatchOnByteArrayReceiver;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.semiHonest.OTSemiHonestDDHBatchOnByteArraySender;

/**
 * This factory produces {@link BaseOTSender}'s and {@link BaseOTReceiver}'s,
 * which are wrappers around SCAPI's
 * {@link OTSemiHonestDDHBatchOnByteArraySender} and
 * {@link OTSemiHonestDDHBatchOnByteArrayReceiver} resp.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class BaseOTFactory implements OTFactory {

	private Network network;
	private int myId;
	private SecureRandom random;

	public BaseOTFactory(Network network, int myId, SecureRandom random) {
		this.network = network;
		this.myId = myId;
		this.random = random;
	}
	
	@Override
	public OTSender createOTSender() {
		return new BaseOTSender(network, myId, random);
	}

	@Override
	public OTReceiver createOTReceiver() {
		return new BaseOTReceiver(network, myId, random);
	}

}
