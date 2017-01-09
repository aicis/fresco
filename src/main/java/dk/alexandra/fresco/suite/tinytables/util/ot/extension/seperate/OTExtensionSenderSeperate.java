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
package dk.alexandra.fresco.suite.tinytables.util.ot.extension.seperate;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import dk.alexandra.fresco.suite.tinytables.util.ot.extension.OTExtensionConfig;
import edu.biu.scapi.comm.Party;

public class OTExtensionSenderSeperate implements OTSender {

	private Party party;

	/**
	 * Create a new OTExtensionSenderSeperate running on the specified address
	 * and port.
	 * 
	 * @param party
	 */
	public OTExtensionSenderSeperate(Party party) {
		this.party = party;
	}

	@Override
	public void send(List<OTInput> inputs) {

		if (inputs.size() > OTExtensionConfig.MAX_OTS) {
			send(inputs.subList(0, OTExtensionConfig.MAX_OTS));
			send(inputs.subList(OTExtensionConfig.MAX_OTS, inputs.size()));
		} else {

			byte[] input0 = Encoding.encodeBooleans(OTInput.getAll(inputs, 0));
			String base64input0 = Base64.getEncoder().encodeToString(input0);

			byte[] input1 = Encoding.encodeBooleans(OTInput.getAll(inputs, 1));
			String base64input1 = Base64.getEncoder().encodeToString(input1);

			ProcessBuilder builder = new ProcessBuilder(OTExtensionConfig.SCAPI_CMD, "-cp",
					OTExtensionConfig.CLASSPATH, OTExtensionConfig.OT_SENDER,
					party.getIpAddress().getHostName(), Integer.toString(party.getPort()), base64input0,
					base64input1);

			String path = new File("") + OTExtensionConfig.PATH;

			builder.directory(new File(path));
			Process p;
			try {
				p = builder.start();
				p.waitFor();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
