/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.lr15;


import java.io.File;

import edu.biu.protocols.yao.primitives.CheatingRecoveryCircuitCreator;
import edu.biu.protocols.yao.primitives.CircuitInput;
import edu.biu.protocols.yao.primitives.CommunicationConfig;
import edu.biu.protocols.yao.primitives.CryptoPrimitives;
import edu.biu.scapi.circuits.circuit.BooleanCircuit;
import edu.biu.scapi.comm.Party;
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.otExtension.OTExtensionMaliciousSender;


/**
 * Tests the LR15 protocol suite in SCAPI 2.4.0, player 1.
 * 
 */
public class LR15Player1Offline {

	
	public LR15Player1Offline() throws Exception {

		System.out.println("Hello, I'm player 1");
		
		int PARTY = 1;
		String HOME_DIR = "/Users/tpj/fresco/src/test/java/dk/alexandra/fresco/suite/lr15"; // TODO: Fix this!
		String circuitFile = HOME_DIR + "/NigelAes.txt";
		String circuitInputFile = HOME_DIR + "/AESPartyOneInputs.txt";
		String crCircuitFile = HOME_DIR + "/UnlockP1Input.txt";
		String COMM_CONFIG_FILENAME = HOME_DIR + "/Parties0.properties";
		String mainBucketsPrefix;
		String crBucketsPrefix;
		
		BooleanCircuit mainCircuit;
		BooleanCircuit crCircuit;

		CommunicationConfig commConfig = new CommunicationConfig(COMM_CONFIG_FILENAME);
		
		CryptoPrimitives primitives = CryptoPrimitives.defaultPrimitives(8); // 8 threads
		commConfig.connectToOtherParty(1 + primitives.getNumOfThreads());
		
		// we read the circuit and this party's input from file
		mainCircuit = new BooleanCircuit(new File(circuitFile));
		CircuitInput input = CircuitInput.fromFile(circuitInputFile, mainCircuit, PARTY);
	
		crCircuit = (new CheatingRecoveryCircuitCreator(crCircuitFile, input.size())).create();
		OTExtensionMaliciousSender otSender = initMaliciousOtSender(mainCircuit.getNumberOfInputs(2), commConfig);

		System.out.println("Player 1 done reading circuits.");
		
	}
	
	
	public void run() throws Exception {
	
	}
	
	
	
	/**
	 * Initializes the malicious OT sender.
	 * @param numOts The number of OTs to run.
	 */
	private OTExtensionMaliciousSender initMaliciousOtSender(int numOts, CommunicationConfig communication) {
		//Get the data of the OT server.
		Party maliciousOtServer = communication.maliciousOtServer();
		String serverAddress = maliciousOtServer.getIpAddress().getHostAddress();
		int serverPort = maliciousOtServer.getPort();
		
		//Create the Malicious OT sender instance.
		return new OTExtensionMaliciousSender(serverAddress, serverPort, numOts);
	}
	


}
