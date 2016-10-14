package dk.alexandra.fresco.suite.tinytables.util.ot;

import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;

public interface OTReceiver {
	
	public boolean[] receive(OTSigma[] sigmas);
	
}
