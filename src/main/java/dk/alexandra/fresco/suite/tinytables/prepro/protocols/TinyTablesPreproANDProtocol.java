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

import java.io.Serializable;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTable;
import dk.alexandra.fresco.suite.tinytables.util.Encoding;
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

		TinyTablesPreproProtocolSuite ps = TinyTablesPreproProtocolSuite.getInstance(resourcePool.getMyId());

		switch (round) {
			case 0:
				if (resourcePool.getMyId() == 1) {
					network.expectInputFromPlayer(2);
					return EvaluationStatus.HAS_MORE_ROUNDS;
				} else {
					/*
					 * The receiver (player 2) uses his shares of the right and
					 * left input resp. as sigmas in the two OT's.
					 */
					otReceiver = new OTReceiver();
					boolean[] sigmas = new boolean[] { inRight.getShare(), inLeft.getShare() };
					Serializable firsts = otReceiver.createFirstMessages(sigmas);
					network.send(1, firsts);
					return EvaluationStatus.HAS_MORE_ROUNDS;
				}

			case 1:
				if (resourcePool.getMyId() == 1) {
					boolean rO = RandomSourceImpl.getInstance().getRandomBoolean();
					out.setShare(rO);

					boolean[] s = RandomSourceImpl.getInstance().getRandomBooleans(4);
					TinyTable tinyTableA = new TinyTable(s);
					ps.getStorage().storeTinyTable(id, tinyTableA);

					boolean m = RandomSourceImpl.getInstance().getRandomBoolean();
					boolean input00 = s[0] ^ rO ^ (inLeft.getShare() && inRight.getShare()) ^ m;
					boolean input01 = input00 ^ inLeft.getShare();
					
					boolean input10 = m; 
					boolean input11 = input10 ^ inRight.getShare();					
					
					Serializable firsts = network.receive(2);
					otSender = new OTSender();
					Serializable seconds = otSender.createSecondMessages(firsts,
							new byte[][] {new byte[] {Encoding.encodeBoolean(input00)}, new byte[] {Encoding.encodeBoolean(input10)}},
							new byte[][] {new byte[] {Encoding.encodeBoolean(input01)}, new byte[] {Encoding.encodeBoolean(input11)}});
					network.send(2, seconds);
										
					boolean[] toSend = new boolean[3];
					toSend[0] = s[0] ^ s[1] ^ inLeft.getShare();
					toSend[1] = s[0] ^ s[2] ^ inRight.getShare();
					toSend[2] = s[0] ^ s[3] ^ inLeft.getShare() ^ inRight.getShare();
					
					network.send(2, toSend);
					return EvaluationStatus.IS_DONE;
				} else {
					network.expectInputFromPlayer(1);
					network.expectInputFromPlayer(1);
					return EvaluationStatus.HAS_MORE_ROUNDS;
				}
				
			case 2:
				if (resourcePool.getMyId() == 1) {
					// Do nothing...
					return EvaluationStatus.IS_DONE;
				} else {
					boolean rO = RandomSourceImpl.getInstance().getRandomBoolean();
					this.out.setShare(rO);
					
					Serializable seconds = network.receive(1);
					byte[][] inputs = otReceiver.finalize(seconds);

					boolean input0 = Encoding.decodeBoolean(inputs[0][0]);
					boolean input1 = Encoding.decodeBoolean(inputs[1][0]);
					
					boolean[] t = new boolean[4];
					t[0] = input0 ^ input1 ^ (inLeft.getShare() && inRight.getShare()) ^ rO; 

					boolean[] shares = network.receive(1);
					t[1] = t[0] ^ shares[0] ^ inLeft.getShare();
					t[2] = t[0] ^ shares[1] ^ inRight.getShare();
					t[3] = t[0] ^ shares[2] ^ inLeft.getShare() ^ inRight.getShare() ^ true;
					
					TinyTable tinyTableB = new TinyTable(t);					
					ps.getStorage().storeTinyTable(id, tinyTableB);
					
					return EvaluationStatus.IS_DONE;
				}

			default:
				throw new MPCException("Cannot evaluate more than two rounds");
		}
	}
}
