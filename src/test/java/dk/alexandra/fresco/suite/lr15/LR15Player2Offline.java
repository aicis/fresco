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
import edu.biu.scapi.interactiveMidProtocols.ot.otBatch.otExtension.OTExtensionMaliciousReceiver;


/**
 * Tests the LR15 protocol suite in SCAPI 2.4.0, player 1.
 * 
 */
public class LR15Player2Offline {


	public LR15Player2Offline() throws Exception {
		System.out.println("Hello, I'm player 2");
		
		int PARTY = 2;
		String HOME_DIR = "/Users/tpj/fresco/src/test/java/dk/alexandra/fresco/suite/lr15"; // TODO: Fix this!
		String COMM_CONFIG_FILENAME = HOME_DIR + "/Parties1.properties";
		String circuitFile = HOME_DIR + "/NigelAes.txt";;
		String circuitInputFile = HOME_DIR + "/AESPartyTwoInputs.txt";
		String crCircuitFile = HOME_DIR + "/UnlockP1Input.txt";
		
		
		String mainBucketsPrefix;
		String crBucketsPrefix;
		
		// we read the circuit and this party's input from file
		BooleanCircuit mainCircuit = new BooleanCircuit(new File(circuitFile));
		CircuitInput input = CircuitInput.fromFile(circuitInputFile, mainCircuit, PARTY);
		BooleanCircuit crCircuit = (new CheatingRecoveryCircuitCreator(crCircuitFile, input.size())).create();;

	
		String mainMatrix;
		String crMatrix;

	
		CommunicationConfig commConfig = new CommunicationConfig(COMM_CONFIG_FILENAME);
		
		CryptoPrimitives primitives = CryptoPrimitives.defaultPrimitives(8);
		commConfig.connectToOtherParty(1 + primitives.getNumOfThreads());


		OTExtensionMaliciousReceiver otReceiver = initMaliciousOtReceiver(mainCircuit.getNumberOfInputs(2), commConfig);

		System.out.println("Player 2 done reading circuits.");
		
	}
	
	
	/**
	 * Initializes the malicious OT receiver.
	 * @param numOts The number of OTs to run.
	 */
	private OTExtensionMaliciousReceiver initMaliciousOtReceiver(int numOts, CommunicationConfig communication) {
		//Get the ip and port of the receiver.
		Party maliciousOtServer = communication.maliciousOtServer();
		String serverAddress = maliciousOtServer.getIpAddress().getHostAddress();
		int serverPort = maliciousOtServer.getPort();
		//Create the malicious OT receiver using the ip, port and number of OTs.
		return new OTExtensionMaliciousReceiver(serverAddress, serverPort, numOts);
	}
		
		
		
		
		
	
	
	
	void run() throws Exception {
	
	}

	


}
