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
package dk.alexandra.fresco.framework.sce.evaluator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetworkImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

/**
 * This class implements the core of a general batched communication strategy 
 * for evaluating Protocols. In this strategy a number of Protocols will be 
 * evaluated round by round in such a way that the communication of all Protocols 
 * is collected and batched together between rounds. More precisely the process 
 * is as follows for a batch of Protocols:
 * 
 * 1. Evaluate the next round of all Protocols and collect messages 
 * to be sent in this round.
 * 
 * 2. Send all messages collected in step 1.
 * 
 * 3. Recieve all messages expected before the next round.
 * 
 * 4. If there are Protocols that are not done start over at step 1.
 * 
 * The processing is done is in a sequential manner (i.e. no parallelization). 
 *
 */
public class BatchedStrategy {

	/**
	 * @param protocols array holding the protocols to be evaluated
	 * 
	 * @param numProtocols the number of protocols in the protocols array to 
	 * evaluate. I.e., protocols[0]...protocols[numProtocols-1] will should be 
	 * evaluated.
	 *  
	 * @param sceNetworks array of sceNetworks corresponding to the protocols to
	 *  be evaluated. I.e., the array should contain numProtocols SCENetworks, 
	 *  with sceNetwork[i] used for communication in protocols[i].
	 * 
	 * @param channel string indicating the channel to communicate over.
	 * 
	 * @param rp  the resource pool.
	 * 
	 * @throws IOException
	 */
	public static void processBatch(NativeProtocol[] protocols, int numProtocols, 
			SCENetworkImpl[] sceNetworks, String channel, ResourcePool rp) throws IOException {
		Network network = rp.getNetwork();
		int round = 0;
		int numParties = rp.getNoOfParties();
		boolean[] doneProtocol = new boolean[numProtocols];
		boolean doneBatch = false;
		// while loop looping over rounds		
		do {
			// The "outgoing" list holds one list of outgoing messages of this 
			// round for each party in the system (i.e., it is a list of 
			// lists of messages).
			// A message is simply an array of Serializable objects.
			// The list contains at most one message for each Protocol
			// evaluated.
			List<List<Serializable[]>> outgoing = new ArrayList<List<Serializable[]>>(numParties);
			Set<Integer> responders = new HashSet<Integer>(numParties);
			for (int i = 0; i < numParties; i++) {
				outgoing.add(new ArrayList<Serializable[]>());
			}			
			doneBatch = true;
			// Step 1: For each protocol evaluate a round of each protocol and 
			// collect outgoing messages.
			for (int i = 0; i < numProtocols; i++) {
				if (!doneProtocol[i]) {					
					EvaluationStatus status = protocols[i].evaluate(round, rp, sceNetworks[i]);
					doneProtocol[i] = status.equals(EvaluationStatus.IS_DONE);
					doneBatch = doneProtocol[i] && doneBatch;				
					// After evaluating the protocol round add the output of this 
					// round to the outgoing lists of the appropriate parties.
					Map<Integer, Queue<Serializable>> output = sceNetworks[i].getOutputFromThisRound();
					for (Map.Entry<Integer, Queue<Serializable>> entry : output.entrySet()) {
						int pId = entry.getKey();
						Queue<Serializable> mesQueue = entry.getValue();
						Serializable[] mesArr = mesQueue.toArray(new Serializable[0]);
						outgoing.get(pId - 1).add(mesArr);
					}
					if (responders.size() < numParties) {
						responders.addAll(sceNetworks[i].getExpectedInputForNextRound());
					}
				}
			}
			// Step 2: Send the lists of messages built above to the appropriate players
			for (int i = 0; i < numParties; i++) {
				List<Serializable[]> mesList = outgoing.get(i);
				if (!mesList.isEmpty()) {
					Serializable[][] mesArr = mesList.toArray(new Serializable[0][0]);
					network.send(channel, i + 1, mesArr);
				}
			}			
			// Step 3: Receive incomming messages from the other parties
			List<Serializable[][]> incomming = new ArrayList<Serializable[][]>(numParties);
			for (int i = 0; i < numParties; i++) {
				if (responders.contains(i + 1)) {
					Serializable[][] mesArr = (Serializable[][]) network.receive(channel, i + 1);
					incomming.add(mesArr);
				} else {
					incomming.add(null);
				}
			}
			// Step 4: Setup input for next round of protocols
			int[] mesCount = new int[numParties];
			for (int i = 0; i < numProtocols; i++) {
				if (!doneProtocol[i]) {
					Map<Integer, Queue<Serializable>> inputForThisRound = new HashMap<Integer, Queue<Serializable>>();
					for (int j = 0; j < numParties; j++) {
						if (sceNetworks[i].getExpectedInputForNextRound().contains(j + 1)) {
							Serializable[] messArr = (incomming.get(j))[mesCount[j]++];
							Queue<Serializable> queue = new LinkedList<Serializable>(Arrays.asList(messArr));
							inputForThisRound.put(j + 1, queue);
						}
					}
					sceNetworks[i].setInput(inputForThisRound);
					sceNetworks[i].nextRound();
				}
			}
			round++;
		} while (!doneBatch);
	}
}
