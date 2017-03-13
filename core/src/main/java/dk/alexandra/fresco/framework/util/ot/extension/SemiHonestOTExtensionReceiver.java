package dk.alexandra.fresco.framework.util.ot.extension;

import java.io.IOException;
import java.util.ArrayList;
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
 * This class represents the receivers part of an implementation of the
 * semi-honest OT Extension as presented in
 * "Extending Oblivious Transfers Efficiently" by Ishai et.al.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SemiHonestOTExtensionReceiver implements OTReceiver {

	private Network network;
	private int myId;
	private int securityParameter;
	private OTFactory baseOT;
	private Random random;

	public SemiHonestOTExtensionReceiver(Network network, int myId,
			int securityParameter, OTFactory baseOT, Random random) {
		this.network = network;
		this.myId = myId;
		this.securityParameter = securityParameter;
		this.baseOT = baseOT;
		this.random = random;
	}

	@Override
	public List<BitSet> receive(List<OTSigma> sigmas, int expectedLength) {

		/*
		 * T is a random binary matrix of size m x k where m is the number of
		 * OTs and k is the security parameter.
		 */
		BinaryMatrix t = BinaryMatrix.getRandomMatrix(sigmas.size(), securityParameter, random);
		
		/*
		 * Make a bitset from the sigmas
		 */
		BitSet r = BitSetUtils.fromList(OTSigma.getAll(sigmas));
		
		/*
		 * The inputs for the base OT's are (t_i, r+t_i) for all columns t_i of
		 * the matrix T.
		 */
		List<OTInput> otInputs= new ArrayList<OTInput>();
		for (int i = 0; i < securityParameter; i++) {
			BitSet x0 = t.getColumn(i);
			BitSet x1 = BitSetUtils.copy(x0);
			x1.xor(r);
			otInputs.add(new OTInput(BitSetUtils.toArray(x0, sigmas.size()),
					BitSetUtils.toArray(x1, sigmas.size())));
		}
		OTSender sender = baseOT.createOTSender();
		sender.send(otInputs);

		BinaryMatrix y;
		try {
			y = network.receive("0", Util.otherPlayerId(myId));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		/*
		 * Y has two columns per sigma. Now for each sigma, the output is z =
		 * Y_sigma + H(j,t).
		 */
		List<BitSet> output = new ArrayList<BitSet>();
		for (int j = 0; j < sigmas.size(); j++) {
			boolean sigma = r.get(j);
			
			/*
			 * If sigma is false, we take the first of the two columns.
			 * Otherwise we take the second.
			 */
			BitSet z = y.getColumn(2*j + (sigma ? 1 : 0));
			z.xor(Util.hash(j, t.getRow(j), expectedLength));
			output.add(z);
		}
		return output;
	}

}
