package dk.alexandra.fresco.suite.tinytables.ot.extension;

import dk.alexandra.fresco.framework.network.Network;
import java.util.Random;

public class SemiHonestOTExtensionFactory implements
    dk.alexandra.fresco.suite.tinytables.ot.OTFactory {

	private Network network;
	private int myId;
	private int securityParameter;
  private dk.alexandra.fresco.suite.tinytables.ot.OTFactory baseOT;
  private Random random;

	public SemiHonestOTExtensionFactory(Network network, int myId,
      int securityParameter, dk.alexandra.fresco.suite.tinytables.ot.OTFactory baseOT,
      Random random) {
    this.network = network;
		this.myId = myId;
		this.securityParameter = securityParameter;
		this.baseOT = baseOT;
		this.random = random;
	}
	
	@Override
  public dk.alexandra.fresco.suite.tinytables.ot.OTSender createOTSender() {
    return new SemiHonestOTExtensionSender(network, myId, securityParameter, baseOT, random);
	}

	@Override
  public dk.alexandra.fresco.suite.tinytables.ot.OTReceiver createOTReceiver() {
    return new SemiHonestOTExtensionReceiver(network, myId, securityParameter, baseOT, random);
	}

}
