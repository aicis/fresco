package dk.alexandra.fresco.suite.tinytables.util.ot.java;

import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.exceptions.FactoriesException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.OTBatchOnByteArrayROutput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.OTBatchRBasicInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.semiHonest.OTSemiHonestDDHBatchOnByteArrayReceiver;
import edu.biu.scapi.tools.Factories.KdfFactory;

public class JavaOTReceiver implements OTReceiver {

	private Network network;
	private int myId;

	public JavaOTReceiver(Network network, int myId) {
		this.network = network;
		this.myId = myId;
	}
	
	@Override
	public List<Boolean> receive(List<OTSigma> sigmas) {
		
		OTSemiHonestDDHBatchOnByteArrayReceiver receiver;
		try {
			receiver = new OTSemiHonestDDHBatchOnByteArrayReceiver(
					new edu.biu.scapi.primitives.dlog.bc.BcDlogECF2m(), KdfFactory.getInstance()
							.getObject("HKDF(HMac(SHA-256))"), new SecureRandom());
		} catch (SecurityLevelException | IOException | FactoriesException e1) {
			return null; // Should not happen
		}

		ArrayList<Byte> s = new ArrayList<Byte>();
		for (OTSigma sigma : sigmas) {
			s.add(Encoding.encodeBoolean(sigma.getSigma()));
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
			
			List<Boolean> results = new ArrayList<Boolean>();
			for (int i = 0; i < sigmas.size(); i++) {
				results.add(Encoding.decodeBoolean(output.getXSigmaArr().get(i)[0]));
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
