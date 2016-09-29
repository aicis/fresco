/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.suite.tinytables.prepro.protocols;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTable;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import dk.alexandra.fresco.suite.tinytables.util.Util;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTFactory;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;

/**
 * <p>
 * This class represents an AND protocol in the preprocessing phase of the
 * TinyTables protocol.
 * </p>
 * 
 * <p>
 * Here, each of the two players picks random shares for the mask of the output
 * wire, <i>r<sub>O</sub></i>. Each player also has to calculate a so called
 * <i>TinyTable</i> for this protocol. Player 1 picks random values for his:
 * </p>
 * <table>
 * <tr>
 * <td><i>t<sub>00</sub></i></td>
 * <td><i>t<sub>01</sub></i></td>
 * </tr>
 * <tr>
 * <td><i>t<sub>10</sub></i></td>
 * <td><i>t<sub>11</sub></i></td>
 * </tr>
 * </table>
 * <p>
 * and player 2 needs to compute a TinyTable which looks like this
 * </p>
 * <table>
 * <tr>
 * <td><i>t<sub>00</sub>+r<sub>O</sub>+r<sub>u</sub>r<sub>v</sub></i></td>
 * <td><i>t<sub>01</sub>+r<sub>O</sub>+r<sub>u</sub>(r<sub>v</sub>+1)</i></td>
 * </tr>
 * <tr>
 * <td><i>t<sub>10</sub>+r<sub>O</sub>+(r<sub>u</sub>+1)r<sub>v</sub></i></td>
 * <td><i>t<sub>11</sub>+r<sub>O</sub>+(r<sub>u</sub>+1)(r<sub>v</sub>+1)</i></td>
 * </tr>
 * </table>
 * <p>
 * This is done using oblivious transfer, but for performance reasons this is
 * not done until the end of the preprocessing phase where all oblivious
 * transfers are done in one batch. So here, player 1 just stores his inputs to
 * oblivious transfer, {@link #calculateOTInputs(TinyTable, boolean)}), and some
 * additional valus needed by player 2 to calculate his TinyTable,
 * {@link #calculateTmps(TinyTable)}.
 * </p>
 * <p>
 * Now, after the oblivious transfers are finished, player 2 can compute his
 * TinyTable using
 * {@link #calculateTinyTable(boolean, boolean, boolean, boolean, boolean, boolean[])}
 * .
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesPreproANDProtocol extends TinyTablesPreproProtocol implements AndProtocol {

	private TinyTablesPreproSBool inLeft, inRight, out;

	public TinyTablesPreproANDProtocol(int id, TinyTablesPreproSBool inLeft,
			TinyTablesPreproSBool inRight, TinyTablesPreproSBool out) {
		super();
		this.id = id;
		this.inLeft = inLeft;
		this.inRight = inRight;
		this.out = out;
	}

	@Override
	public Value[] getInputValues() {
		return new Value[] { inLeft, inRight };
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] { out };
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {

		TinyTablesPreproProtocolSuite ps = TinyTablesPreproProtocolSuite.getInstance(resourcePool
				.getMyId());

		switch (round) {
			case 0:
				if (resourcePool.getMyId() == 1) {
					/*
					 * Player 1
					 */

					// Pick share for output gate
					boolean rO = resourcePool.getSecureRandom().nextBoolean();
					out.setShare(rO);

					/*
					 * Calculate inputs for OT's. The reason for using OTs is
					 * for player 1 to know the 'mixed' terms (eg. terms with
					 * shares from both players) of rU & rV = (rU1 + rU2) & (rV1
					 * + rV2) = rU1 & rV1 + rU2 & rV2 + rU1 & rV2 + rU2 & rV1.
					 * For now we just store the inputs and then do all the OTs
					 * in one batch at the end of the preprocessing phase.
					 */
					boolean x0 = resourcePool.getSecureRandom().nextBoolean();
					boolean x1 = resourcePool.getSecureRandom().nextBoolean();

					OTInput[] otInputs = calculateOTInputs(x0, x1);
					ps.getStorage().storeOTInput(id, otInputs);

					/*
					 * We let the (0,0)'th entry of player 1's TinyTable be rU1
					 * & rU2 + rO1 + x0 + x1, where x0 and x1 are the random
					 * masks from the OT's described above. Now, if player 2 let
					 * the (0,0)'th entry of his TinyTable to be rU2 & rV2 + rU1
					 * & rV2 + rU2 & rV1 + rO2, then the two entries are a
					 * secret sharing of rO + rU & rV.
					 */
					boolean[] entries = new boolean[4];
					entries[0] = inRight.getShare() & inLeft.getShare() ^ rO ^ x0 ^ x1;
					for (int i = 1; i < entries.length; i++) {
						entries[i] = resourcePool.getSecureRandom().nextBoolean();
					}
					TinyTable tinyTable = new TinyTable(entries);
					ps.getStorage().storeTinyTable(id, tinyTable);

					/*
					 * Player two need some additional values to be able to
					 * calculate his TinyTable:
					 */
					boolean[] tmps = calculateTmps(tinyTable);
					ps.getStorage().storeTemporaryBooleans(id, tmps);

					return EvaluationStatus.IS_DONE;
				} else {
					/*
					 * Player 2
					 */

					/*
					 * The receiver (player 2) uses rV2 and rU2 resp. as sigmas
					 * for the two OT's.
					 */
					OTSigma[] sigmas = new OTSigma[] { new OTSigma(inRight.getShare()),
							new OTSigma(inLeft.getShare()) };
					ps.getStorage().storeOTSigma(id, sigmas);

					// Pick share for output gate
					boolean rO = resourcePool.getSecureRandom().nextBoolean();
					out.setShare(rO);
					ps.getStorage().storeMaskShare(id, rO);

					return EvaluationStatus.IS_DONE;
				}

			default:
				throw new MPCException("Cannot evaluate more than one round");
		}
	}

	/**
	 * Calculate some additional values needed by player 2 to calculate his
	 * TinyTable: <i>t<sub>00</sub> + t<sub>01</sub> +
	 * r<sub>U</sub><sup>1</sup>, t<sub>00</sub> + t<sub>10</sub> +
	 * r<sub>V</sub><sup>1</sup></i> and <i>t<sub>00</sub> + t<sub>01</sub> +
	 * r<sub>U</sub><sup>1</sup> + r<sub>V</sub><sup>1</sup></i>.
	 * 
	 * @param t
	 *            Player 1's TinyTable for this protocol
	 * @return
	 */
	private boolean[] calculateTmps(TinyTable t) {
		boolean[] tmps = new boolean[3];
		tmps[0] = t.getValue(false, false) ^ t.getValue(false, true) ^ inLeft.getShare();
		tmps[1] = t.getValue(false, false) ^ t.getValue(true, false) ^ inRight.getShare();
		tmps[2] = t.getValue(false, false) ^ t.getValue(true, true) ^ inLeft.getShare()
				^ inRight.getShare();
		return tmps;
	}

	/**
	 * We use OT's to calculate the products rU1 & rV2 and rU2 & rV1. After the
	 * OT, player 2 knows x0 + rU1 & rV2 and x1 = rU2 & rV1, and player 1 knows
	 * x0 and x1, so they now have secret sharings of the two products.
	 * 
	 * @param x0
	 *            A randomly chosen boolean.
	 * @param x1
	 *            A randomly chosen boolean.
	 * @return
	 */
	private OTInput[] calculateOTInputs(boolean x0, boolean x1) {
		OTInput[] otInputs = new OTInput[2];
		otInputs[0] = new OTInput(x0, x0 ^ inLeft.getShare());
		otInputs[1] = new OTInput(x1, x1 ^ inRight.getShare());
		return otInputs;
	}

	/**
	 * 
	 * @param output0
	 *            Result of first OT for this protocol. Should be equal to
	 *            <i>s<sub>00</sub> + r<sub>O</sub><sup>1</sup> +
	 *            r<sub>U</sub><sup>1</sup> & r<sub>U</sub><sup>1</sup> + m +
	 *            r<sub>U</sub><sup>1</sup> & r<sub>V</sub><sup>2</sup></i>.
	 * @param output1
	 *            Result of second OT for this protocol. Should be equal to <i>m
	 *            + r<sub>U</sub><sup>2</sup> & r<sub>V</sub><sup>1</sup></i>.
	 * @param rU
	 *            Player 2's share of the left input wire,
	 *            <i>r<sub>U</sub><sup>2</sup></i>.
	 * @param rV
	 *            Player 2's share of the right input wire,
	 *            <i>r<sub>U</sub><sup>2</sup></i>.
	 * @param rO
	 *            Player 2's share of the output wire,
	 *            <i>r<sub>O</sub><sup>2</sup></i>.
	 * @param y
	 *            Additional values needed for calculating TinyTable. Player 1
	 *            should send these to player 2 during preprocessing. Should be
	 *            equal to <i>[<i>t<sub>00</sub> + t<sub>01</sub> +
	 *            r<sub>U</sub><sup>1</sup>,t<sub>00</sub> + t<sub>10</sub> +
	 *            r<sub>V</sub><sup>1</sup>, t<sub>00</sub> + t<sub>01</sub> +
	 *            r<sub>U</sub><sup>1</sup> + r<sub>V</sub><sup>1</sup></i>]</i>
	 *            where <i>t</i> is player 1's TinyTable.
	 * @return Player 2's TinyTable such that the <i>entry
	 */
	private static TinyTable calculateTinyTable(boolean output0, boolean output1, boolean rU,
			boolean rV, boolean rO, boolean[] y) {
		boolean[] s = new boolean[4];
		/*
		 * In the comments below, we let t denote player 1's TinyTable and x0
		 * and x1 denote the random masks chosen by player 1 during
		 * preprocessing.
		 */

		/*
		 * s[0] = s_00 = t_00 + rO^1 + rU1 rU1 + x0 + x1 + rU1 rV2 + x0 + rU2
		 * rV1 + x1 + rU2 rV2 + rO^2 = t_00 + rO + rU rV
		 */
		s[0] = output0 ^ output1 ^ (rU && rV) ^ rO;

		/*
		 * s[1] = s_01 = t_00 + rO + rU rV + t_00 + t_01 + rU1 + rU = t_01 + rO
		 * + rU !rV
		 */
		s[1] = s[0] ^ y[0] ^ rU;

		/*
		 * s[2] = s_10 = t_00 + rO + rU rV + t_00 + t_10 + rV1 + rV2 = t_10 + rO
		 * + !rU rV
		 */
		s[2] = s[0] ^ y[1] ^ rV;

		/*
		 * s[3] = s_11 = t_00 + rO + rU & rV + t_00 + t_11 + rU1 + rV1 + rU2 +
		 * rV2 + 1 = t_11 + rO + !rU !rV
		 */
		s[3] = s[0] ^ y[2] ^ rU ^ rV ^ true;

		TinyTable tinyTable = new TinyTable(s);
		return tinyTable;
	}

	/**
	 * Given the outputs after performing OT's with player 1, this method
	 * calculates and stores Player 2's TinyTables for all AND protocols.
	 * 
	 * @param otOutputs
	 * @param storage
	 */
	private static void player2CalculateTinyTables(List<Boolean> otOutputs,
			TinyTablesStorage storage) {
		int progress = 0;
		for (int id : storage.getOTSigmas().keySet()) {

			/*
			 * Two OT's per AND gate
			 */
			boolean rV = storage.getOTSigmas().get(id)[0].getSigma();
			boolean rU = storage.getOTSigmas().get(id)[1].getSigma();
			boolean output0 = otOutputs.get(progress);
			boolean output1 = otOutputs.get(progress + 1);

			/*
			 * We stored our share of r_O and player 1's s_00 + s_01 + rU^1,
			 * s_00 + s_10 + rV^1 and s_00 + s_11 + rU^1 + rV^1 as tmp's during
			 * preprocessing. Here rU^1 is player 1's share of rU (left input
			 * wire) (likewise for rV) and s_ij is the ij'th entry of player 1's
			 * TinyTable.
			 */
			boolean[] y = storage.getTemporaryBooleans().get(id);
			boolean rO = storage.getMaskShare(id);

			TinyTable tinyTable = TinyTablesPreproANDProtocol.calculateTinyTable(output0, output1,
					rU, rV, rO, y);
			storage.storeTinyTable(id, tinyTable);

			/*
			 * For each protocol, we do two OTs so the index needs to be
			 * increased by two.
			 */
			progress += 2;
		}
	}

	public static void finishPreprocessing(int playerId, OTFactory otFactory,
			TinyTablesStorage storage, Network network) {
		switch (playerId) {
			case 1:
				/*
				 * Player 1
				 */
				try {
					network.send("0", 2, (Serializable) storage.getTemporaryBooleans());
				} catch (IOException e) {
					e.printStackTrace();
				}
				OTSender sender = otFactory.createOTSender();
				List<OTInput> inputs = Util.getAll(storage.getOTInputs());
				sender.send(inputs);
				break;

			case 2:
				/*
				 * Player 2
				 */
				try {
					SortedMap<Integer, boolean[]> in = network.receive("0", 1);
					storage.getTemporaryBooleans().putAll(in);
				} catch (IOException e) {
					e.printStackTrace();
				}

				/*
				 * Do OT's with player 1 for all AND gates in the storage. Each
				 * of them has stored two sigmas and player 1 has corresponding
				 * inputs.
				 */
				OTReceiver receiver = otFactory.createOTReceiver();
				List<OTSigma> sigmas = Util.getAll(storage.getOTSigmas());
				List<Boolean> outputs = receiver.receive(sigmas);
				if (outputs.size() < 2 * storage.getOTSigmas().size()) {
					throw new MPCException("To few outputs from OT's: Expected "
							+ storage.getOTSigmas().size() * 2 + " but got only " + outputs.size());
				}

				TinyTablesPreproANDProtocol.player2CalculateTinyTables(outputs, storage);
				break;
		}
	}
}
