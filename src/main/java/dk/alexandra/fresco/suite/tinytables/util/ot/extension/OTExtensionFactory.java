package dk.alexandra.fresco.suite.tinytables.util.ot.extension;

import java.net.InetSocketAddress;

import dk.alexandra.fresco.suite.tinytables.util.ot.OTFactory;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;

public class OTExtensionFactory implements OTFactory {

	private InetSocketAddress address;

	public OTExtensionFactory(InetSocketAddress address) {
		this.address = address;
	}
	
	@Override
	public OTSender createOTSender() {
		return new OTExtensionSender(address);
	}

	@Override
	public OTReceiver createOTReceiver() {
		return new OTExtensionReceiver(address);
	}

}
