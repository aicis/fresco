package dk.alexandra.fresco.suite.tinytables.util.ot;

import java.util.List;

import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;

public interface OTReceiver {
	
	public List<Boolean> receive(List<OTSigma> sigmas);
	
}
