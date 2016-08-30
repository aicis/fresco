package dk.alexandra.fresco.suite.tinytables.util.ot.extension;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import dk.alexandra.fresco.suite.tinytables.util.ot.OTFactory;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;
import edu.biu.scapi.comm.Party;

public class OTExtensionFactory implements OTFactory {

	private boolean seperate;
	private Party party;

	public OTExtensionFactory(InetSocketAddress address, boolean seperate) throws UnknownHostException {
		this.seperate = seperate;
		
		this.party = new Party(InetAddress.getByName(address.getHostName()), address.getPort());
	}

	public OTExtensionFactory(InetSocketAddress address) throws UnknownHostException {
		this(address, false);
	}
	
	@Override
	public OTSender createOTSender() {
		if (seperate) {
			return new OTExtensionSenderSeperate(party);
		} else {
			return new OTExtensionSender(party);
		}
	}

	@Override
	public OTReceiver createOTReceiver() {
		if (seperate) {
			return new OTExtensionReceiverSeperate(party);
		} else {
			return new OTExtensionReceiver(party);
		}
	}

}
