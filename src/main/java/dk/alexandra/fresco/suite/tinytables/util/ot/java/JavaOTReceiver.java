package dk.alexandra.fresco.suite.tinytables.util.ot.java;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.OTBatchOnByteArrayROutput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.OTBatchRBasicInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.semiHonest.OTSemiHonestDDHBatchOnByteArrayReceiver;

public class JavaOTReceiver implements OTReceiver {

	private Network network;
	private int myId;

	public JavaOTReceiver(Network network, int myId) {
		this.network = network;
		this.myId = myId;
	}
	
	@Override
	public boolean[] receive(OTSigma[] sigmas) {
		
		OTSemiHonestDDHBatchOnByteArrayReceiver receiver = new OTSemiHonestDDHBatchOnByteArrayReceiver();

		ArrayList<Byte> s = new ArrayList<Byte>();
		for (int i = 0; i < sigmas.length; i++) {
			s.add(Encoding.encodeBoolean(sigmas[i].getSigma()));
		}
		OTBatchRBasicInput input = new OTBatchRBasicInput(s);
		
		try {
			OTBatchOnByteArrayROutput output = (OTBatchOnByteArrayROutput) receiver.transfer(new Channel() {

				@Override
				public void close() {
					
				}

				@Override
				public boolean isClosed() {
					return false;
				}

				@Override
				public Serializable receive() throws ClassNotFoundException, IOException {
					return network.receive("0", otherPlayerId());
				}

				@Override
				public void send(Serializable arg0) throws IOException {
					network.send("0", otherPlayerId(), arg0);
				}
				
			}, input);
			
			boolean[] results = new boolean[sigmas.length];
			for (int i = 0; i < sigmas.length; i++) {
				results[i] = Encoding.decodeBoolean(output.getXSigmaArr().get(i)[0]);
			}
			
			return results;
		} catch (ClassNotFoundException | IOException e) {
			// Do nothing
		}
		
		return null;
	}
	
	private int otherPlayerId() {
		return myId == 1 ? 2 : 1;
	}

}
