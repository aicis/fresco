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

	public JavaOTSender(Network network, int myId) {
		this.network = network;
		this.myId = myId;
	}
	
	@Override
	public void send(List<OTInput> inputs) {
		
		ArrayList<byte[]> x0 = new ArrayList<byte[]>();
		ArrayList<byte[]> x1 = new ArrayList<byte[]>();
		for (OTInput input : inputs) {
			x0.add(new byte[] {Encoding.encodeBoolean(input.getX0())});
			x1.add(new byte[] {Encoding.encodeBoolean(input.getX1())});
		}
		OTBatchOnByteArraySInput otsInputs = new OTBatchOnByteArraySInput(x0, x1);
		OTSemiHonestDDHBatchOnByteArraySender sender;
		try {
			sender = new OTSemiHonestDDHBatchOnByteArraySender(new edu.biu.scapi.primitives.dlog.bc.BcDlogECF2m(), KdfFactory.getInstance()
					.getObject("HKDF(HMac(SHA-256))"), new SecureRandom());
		} catch (SecurityLevelException | IOException | FactoriesException e1) {
			return; //Should not happen
		}
		
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
