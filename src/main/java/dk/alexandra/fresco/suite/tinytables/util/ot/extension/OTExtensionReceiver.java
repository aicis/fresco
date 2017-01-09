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
package dk.alexandra.fresco.suite.tinytables.util.ot.extension;

import java.util.List;

import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;
import edu.biu.scapi.comm.Party;
import edu.biu.scapi.interactiveMidProtocols.ot.OTOnByteArrayROutput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.OTBatchRInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.otExtension.OTExtensionGeneralRInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.otExtension.OTSemiHonestExtensionReceiver;

public class OTExtensionReceiver implements OTReceiver {

	private Party party;

	/**
	 * Create new OTExtensionReceiver with an OTExtensionSender running at the
	 * specified party.
	 * 
	 * @param party
	 */
	public OTExtensionReceiver(Party party) {
		this.party = party;
	}

	@Override
	public List<Boolean> receive(List<OTSigma> sigmas) {
		byte[] binarySigmas = Encoding.encodeBooleans(OTSigma.getAll(sigmas));

		OTSemiHonestExtensionReceiver receiver = new OTSemiHonestExtensionReceiver(party);

		OTBatchRInput input = new OTExtensionGeneralRInput(binarySigmas, 8);
		OTOnByteArrayROutput output = (OTOnByteArrayROutput) receiver.transfer(null, input);
		return Encoding.decodeBooleans(output.getXSigma());
	}

}
