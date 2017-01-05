package dk.alexandra.fresco.suite.tinytables.util.ot.java;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import edu.biu.scapi.exceptions.FactoriesException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.OTBatchOnByteArraySInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.semiHonest.OTSemiHonestDDHBatchOnByteArraySender;
import edu.biu.scapi.tools.Factories.KdfFactory;

/**
 * This OTSender is a wrapper around SCAPI's
 * {@link OTSemiHonestDDHBatchOnByteArraySender} based on an elliptic curve over
 * a finite field, namely the K-163 curve.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class JavaOTSender implements OTSender {

	private Network network;
	private int myId;
	private SecureRandom random;

	/*
	 * We keep a singleton of the actual sender.
	 */
	private static OTSemiHonestDDHBatchOnByteArraySender sender;
	private OTSemiHonestDDHBatchOnByteArraySender getInstance(SecureRandom random) {
		if (sender == null) {
			try {
				sender = new OTSemiHonestDDHBatchOnByteArraySender(
						//new edu.biu.scapi.primitives.dlog.bc.BcDlogECF2m(), 
						new edu.biu.scapi.primitives.dlog.openSSL.OpenSSLDlogECF2m(), 
						KdfFactory.getInstance()
						.getObject("HKDF(HMac(SHA-256))"), random);
			} catch (SecurityLevelException | FactoriesException | IOException e) {
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
			sender.transfer(new NetworkWrapper(network, myId), otsInputs);
		} catch (ClassNotFoundException | IOException e) {
			// Do nothing
		}
		
	}
	
}
