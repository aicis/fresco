package dk.alexandra.fresco.framework.util.ot.extension;

import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.BinaryMatrix;
import dk.alexandra.fresco.framework.util.BitSetUtils;
import dk.alexandra.fresco.framework.util.ot.OTFactory;
import dk.alexandra.fresco.framework.util.ot.OTReceiver;
import dk.alexandra.fresco.framework.util.ot.OTSender;
import dk.alexandra.fresco.framework.util.ot.datatypes.OTInput;
import dk.alexandra.fresco.framework.util.ot.datatypes.OTSigma;
import dk.alexandra.fresco.suite.tinytables.util.Util;

/**
 * This class represents the senders part of an implementation of the
 * semi-honest OT Extension as presented in
 * "Extending Oblivious Transfers Efficiently" by Ishai et.al.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SemiHonestOTExtensionSender implements OTSender {

	private Network network;
	private int myId;
	private int securityParameter;
	private OTFactory baseOT;
	private Random random;

	public SemiHonestOTExtensionSender(Network network, int myId, int securityParameter,
			OTFactory baseOT, Random random) {
		this.network = network;
		this.myId = myId;
		this.securityParameter = securityParameter;
		this.baseOT = baseOT;
		this.random = random;
	}

	@Override
	public void send(List<OTInput> inputs) {
		
		// We assume that all inputs have same length.
		int stringLength = inputs.get(0).getLength();
		
		/*
		 * Randomize selection bits for base OT's
		 */
		BitSet s = BitSetUtils.getRandomBits(securityParameter, random);
		
		OTReceiver receiver = baseOT.createOTReceiver();
		List<OTSigma> sigmas = OTSigma.fromList(BitSetUtils.toList(s, securityParameter));

		/*
		 * The output of the OT's are used as columns of a matrix Q
		 */
		List<BitSet> otOutput = receiver.receive(sigmas, inputs.size());
		BinaryMatrix q = BinaryMatrix.fromColumns(otOutput, inputs.size());
		
		/*
		 * We build a matrix Y with two columns for each OTInput:
		 */
		BinaryMatrix y = new BinaryMatrix(stringLength, 2*inputs.size());
		for (int i = 0; i < inputs.size(); i++) {
			
			/*
			 * First column for an input i is x0 + H(i, q_i)...
			 */
			BitSet x0 = inputs.get(i).getX0();
			BitSet row = q.getRow(i);
			x0.xor(Util.hash(i, row, stringLength));
			y.setColumn(2*i, x0);
			
			/*
			 * ...and the second column is x1 + H(i, q_i) + s.
			 */
			BitSet x1 = inputs.get(i).getX1();
			row.xor(s);
			x1.xor(Util.hash(i, row, stringLength));			
			y.setColumn(2*i + 1, x1);
		}
		
		try {
			network.send("0", Util.otherPlayerId(myId), y);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
}
