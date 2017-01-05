package dk.alexandra.fresco.suite.tinytables.util.ot.java;

import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
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
	private SecureRandom random;

	private static OTSemiHonestDDHBatchOnByteArrayReceiver receiver;
	private OTSemiHonestDDHBatchOnByteArrayReceiver getInstance(SecureRandom random) {
		if (receiver == null) {
			try {
				receiver = new OTSemiHonestDDHBatchOnByteArrayReceiver(
						new edu.biu.scapi.primitives.dlog.bc.BcDlogECF2m(), KdfFactory.getInstance()
						.getObject("HKDF(HMac(SHA-256))"), random);
			} catch (SecurityLevelException | IOException | FactoriesException e) {
				e.printStackTrace();
			}
		} 
		return receiver;
	}
	
	
	
	public JavaOTReceiver(Network network, int myId, SecureRandom random) {
		this.network = network;
		this.myId = myId;
		this.random = random;
	}
	
	@Override
	public List<boolean[]> receive(List<OTSigma> sigmas, int expectedLength) {
		
		OTSemiHonestDDHBatchOnByteArrayReceiver receiver = getInstance(random);

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
			
			List<boolean[]> results = new ArrayList<boolean[]>();
			for (int i = 0; i < sigmas.size(); i++) {
				results.add(Arrays.copyOf(Encoding.decodeBooleans(output.getXSigmaArr().get(i)), expectedLength));
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
