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

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTable;
import dk.alexandra.fresco.suite.tinytables.util.RandomSourceImpl;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;

public class TinyTablesPreproANDProtocol extends TinyTablesPreproProtocol implements AndProtocol {

	private int id;
	private TinyTablesPreproSBool inLeft, inRight, out;

	OTReceiver otReceiver;
	OTSender otSender;

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
					boolean rO = RandomSourceImpl.getInstance().getRandomBoolean();
					out.setShare(rO);
					
					// Pick random entries for TinyTable
					boolean[] s = RandomSourceImpl.getInstance().getRandomBooleans(4);

					TinyTable tinyTable = new TinyTable(s);
					ps.getStorage().storeTinyTable(id, tinyTable);

					// Create inputs for OT's
					boolean m = RandomSourceImpl.getInstance().getRandomBoolean();
					boolean[][] otInputs = new boolean[2][2];
					otInputs[0][0] = s[0] ^ rO ^ (inLeft.getShare() && inRight.getShare()) ^ m;
					otInputs[0][1] = otInputs[0][0] ^ inLeft.getShare();
					otInputs[1][0] = m;
					otInputs[1][1] = otInputs[1][0] ^ inRight.getShare();
					ps.getStorage().storeOTInput(id, otInputs);

					// Send
					boolean[] y = new boolean[3];
					y[0] = s[0] ^ s[1] ^ inLeft.getShare();
					y[1] = s[0] ^ s[2] ^ inRight.getShare();
					y[2] = s[0] ^ s[3] ^ inLeft.getShare() ^ inRight.getShare();
					network.send(2, y);

					return EvaluationStatus.IS_DONE;
				} else {
					/*
					 * Player 2
					 */

					/*
					 * The receiver (player 2) uses his shares of the right and
					 * left input resp. as sigmas in the two OT's.
					 */
					boolean[] sigmas = new boolean[] { inRight.getShare(), inLeft.getShare() };
					ps.getStorage().storeOTSigma(id, sigmas);

					network.expectInputFromPlayer(1);

					return EvaluationStatus.HAS_MORE_ROUNDS;
				}

			case 1:
				if (resourcePool.getMyId() == 1) {
					/*
					 * Player 1
					 */
					
					// Already finished - ignore
					return EvaluationStatus.IS_DONE;
				} else {
					/*
					 * Player 2
					 */
					
					boolean[] received = network.receive(1);
					
					// Pick share for output gate
					boolean rO = RandomSourceImpl.getInstance().getRandomBoolean();
					out.setShare(rO);

					boolean[] tmps = new boolean[received.length + 1];
					tmps[0] = rO;
					for (int i = 0; i < received.length; i++) {
						tmps[i+1] = received[i];
					}
					
					// Store [rO, y0, y1, y2] as tmps where 
					ps.getStorage().storeTemporaryBooleans(id, tmps);
					return EvaluationStatus.IS_DONE;
				}

			default:
				throw new MPCException("Cannot evaluate more than one round");
		}
	}
}
