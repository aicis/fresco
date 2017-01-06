package dk.alexandra.fresco.framework.util.ot.extension.semihonest;

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
		 * Randomize sigmas
		 */
		BitSet s = BitSetUtils.getRandomBits(securityParameter, random);
		
		OTReceiver receiver = baseOT.createOTReceiver();
		List<OTSigma> sigmas = OTSigma.fromList(BitSetUtils.toList(s, securityParameter));
	
		/*
		 * The output of  Columns of q
		 */
		List<boolean[]> otOutput = receiver.receive(sigmas, inputs.size());
		BinaryMatrix q = fromListOfColumns(otOutput);
		
		BinaryMatrix y = new BinaryMatrix(stringLength, 2*inputs.size());
		for (int i = 0; i < inputs.size(); i++) {
			BitSet x0 = BitSetUtils.fromArray(inputs.get(i).getX0());
			BitSet x1 = BitSetUtils.fromArray(inputs.get(i).getX1());
			
			BitSet row = q.getRow(i);
			
			x0.xor(Util.hash(i, row, stringLength));
			y.setColumn(2*i, x0);
			
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
	
	private BinaryMatrix fromListOfColumns(List<boolean[]> columns) {
		BinaryMatrix matrix = new BinaryMatrix(columns.get(0).length, columns.size());
		for (int j = 0; j < matrix.getWidth(); j++) {
			BitSet column = BitSetUtils.fromArray(columns.get(j));
			matrix.setColumn(j, column);
		}
		return matrix;
	}
	
}
