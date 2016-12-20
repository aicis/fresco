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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.List;

import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;
import dk.alexandra.fresco.suite.tinytables.util.ot.extension.OTExtensionReceiver;
import edu.biu.scapi.comm.Party;

/**
 * Perform Oblivious transfer extension as the receiving part. Should be called
 * with three parameters: The host address of the sender, the port number and the sigmas as a
 * Base64-encoded byte-array with 0x00 = false and 0x01 = true.
 * 
 * @author jonas
 *
 */
public class OTExtensionReceiverApplication {

	public static void main(String[] args) throws UnknownHostException {

		String host = args[0];
		String portAsString = args[1];
		String sigmasBase64 = args[2];

		int port;
		try {
			port = Integer.parseInt(portAsString);
		} catch (NumberFormatException e) {
			System.out.println("Invalid port number: " + portAsString);
			System.exit(-1);
			return;
		}
		
		byte[] sigmas;
		try {
			sigmas = Base64.getDecoder().decode(sigmasBase64);
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid sigmas array: " + sigmasBase64);
			System.exit(-1);
			return;
		}
		byte[] output = receive(host, port, sigmas);
		String outputBase64 = Base64.getEncoder().encodeToString(output);
		System.out.println(outputBase64);
	}
	
	public static byte[] receive(String host, int port, byte[] sigmas) throws UnknownHostException {
		
		OTExtensionReceiver receiver = new OTExtensionReceiver(new Party(InetAddress.getByName(host), port));
		List<OTSigma> otSigmas = OTSigma.fromList(Encoding.decodeBooleans(sigmas));
		List<Boolean> outputs = receiver.receive(otSigmas);
				
		return Encoding.encodeBooleans(outputs);
	}

}
