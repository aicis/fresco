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
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import edu.biu.scapi.comm.Party;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.otExtension.OTExtensionGeneralSInput;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.otExtension.OTSemiHonestExtensionSender;

/**
 * We use SCAPI's OT Extension library for doing oblivious transfers. However,
 * this lib is implemented in C++ and we need to call it using JNI. Also, for
 * testing we run the different players on the same machine in two different
 * threads which seem to cause problems with the lib, so to avoid that we run
 * the JNI in seperate processes.
 * 
 * @author jonas
 *
 */
public class OTExtensionSender implements OTSender {

	private Party party;

	/**
	 * Create a new OTExtensionSender. The given <code>party</code> is the ip
	 * and port address we should be listening on (eg. this party's address).
	 * 
	 * @param party
	 */
	public OTExtensionSender(Party party) {
		this.party = party;
	}

	@Override
	public void send(List<OTInput> inputs) {

		byte[] input0 = Encoding.encodeBooleans(OTInput.getAll(inputs, 0));
		byte[] input1 = Encoding.encodeBooleans(OTInput.getAll(inputs, 1));
		
		OTSemiHonestExtensionSender sender = new OTSemiHonestExtensionSender(party);
		OTExtensionGeneralSInput input = new OTExtensionGeneralSInput(input0, input1, inputs.size());
		sender.transfer(null, input);
	}

}
