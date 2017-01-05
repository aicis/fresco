package dk.alexandra.fresco.suite.tinytables.util.ot.java;

import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.exceptions.FactoriesException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.OTBatchOnByteArraySInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.semiHonest.OTSemiHonestDDHBatchOnByteArraySender;
import edu.biu.scapi.tools.Factories.KdfFactory;

public class JavaOTSender implements OTSender {

	private Network network;
	private int myId;
	private SecureRandom random;

	private static OTSemiHonestDDHBatchOnByteArraySender sender;
	private OTSemiHonestDDHBatchOnByteArraySender getInstance(SecureRandom random) {
		if (sender == null) {
			try {
				sender = new OTSemiHonestDDHBatchOnByteArraySender(
						new edu.biu.scapi.primitives.dlog.bc.BcDlogECF2m(), KdfFactory.getInstance()
						.getObject("HKDF(HMac(SHA-256))"), random);
			} catch (SecurityLevelException | IOException | FactoriesException e) {
				e.printStackTrace();
			}
		}
		return sender;
	}
	
	public JavaOTSender(Network network, int myId, SecureRandom random) {
		this.network = network;
		this.myId = myId;
		this.random = random;
	}
	
	@Override
	public void send(List<OTInput> inputs) {
		
		ArrayList<byte[]> x0 = new ArrayList<byte[]>();
		ArrayList<byte[]> x1 = new ArrayList<byte[]>();
		for (OTInput input : inputs) {
			byte[] x0i = Encoding.encodeBooleans(input.getX0());
			x0.add(x0i);
			
			byte[] x1i = Encoding.encodeBooleans(input.getX1());
			x1.add(x1i);
		}
		
		
		OTBatchOnByteArraySInput otsInputs = new OTBatchOnByteArraySInput(x0, x1);
		OTSemiHonestDDHBatchOnByteArraySender sender = getInstance(random);
		
		try {
			sender.transfer(new Channel() {

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
				public void send(Serializable otInputs) throws IOException {
					network.send("0", otherPlayerId(), otInputs);
				}
				
			}, otsInputs);
		} catch (ClassNotFoundException | IOException e) {
			// Do nothing
		}
		
	}

	private int otherPlayerId() {
		return myId == 1 ? 2 : 1;
	}

	
}
