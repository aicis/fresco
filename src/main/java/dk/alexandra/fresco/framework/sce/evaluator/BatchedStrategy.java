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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetworkImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

/**
 * This class implements the core of a general batched communication strategy
 * for evaluating Protocols. In this strategy a number of Protocols will be
 * evaluated round by round in such a way that the communication of all
 * Protocols is collected and batched together between rounds. More precisely
 * the process is as follows for a batch of Protocols:
 * 
 * 1. Evaluate the next round of all Protocols and collect messages to be sent
 * in this round.
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
	 * @param protocols
	 *            array holding the protocols to be evaluated
	 * 
	 * @param numProtocols
	 *            the number of protocols in the protocols array to evaluate.
	 *            I.e., protocols[0]...protocols[numProtocols-1] will should be
	 *            evaluated.
	 * 
	 * @param sceNetworks
	 *            array of sceNetworks corresponding to the protocols to be
	 *            evaluated. I.e., the array should contain numProtocols
	 *            SCENetworks, with sceNetwork[i] used for communication in
	 *            protocols[i].
	 * 
	 * @param channel
	 *            string indicating the channel to communicate over.
	 * 
	 * @param rp
	 *            the resource pool.
	 * 
	 * @throws IOException
	 */
	public static void processBatch(NativeProtocol[] protocols, int numOfProtocols, SCENetworkImpl[] sceNetworks,
			String channel, ResourcePool rp) throws IOException {
		Network network = rp.getNetwork();
		int round = 0;
		Set<Integer> partyIds = new HashSet<Integer>();
		for (int i = 1; i <= rp.getNoOfParties(); i++) {
			partyIds.add(i);
		}		
		boolean[] dones = new boolean[numOfProtocols];
		boolean done;
		// Do all rounds
		do {
			// Evaluate the current round for all protocols
			done = true;
			for (int i = 0; i < numOfProtocols; i++) {
				SCENetworkImpl sceNetwork = sceNetworks[i];
				if (!dones[i]) {
					EvaluationStatus status = protocols[i].evaluate(round, rp, sceNetwork);
					if (status.equals(EvaluationStatus.IS_DONE)) {
						dones[i] = true;
					} else {
						done = false;
					}
				}
			}
			// Send/Receive data for this round
			ArrayList<Map<Integer, Queue<byte[]>>> inputs = new ArrayList<Map<Integer, Queue<byte[]>>>(numOfProtocols);
			for (int i = 0; i < numOfProtocols; i++) {
				inputs.add(new HashMap<Integer, Queue<byte[]>>());
			}
			for (int pId = 1; pId <= rp.getNoOfParties(); pId++) {
				// If the current player id is you send your messages
				if (pId == rp.getMyId()) { 
					for (int i = 0; i < numOfProtocols; i++) {
						SCENetworkImpl sceNet = sceNetworks[i];
						Map<Integer, Queue<byte[]>> output = sceNet.getOutputFromThisRound();
						for (Map.Entry<Integer, Queue<byte[]>> e: output.entrySet()) {
							network.send(channel, e.getKey(), e.getValue().size());
							for (Serializable s: e.getValue()) {
								network.send(channel, e.getKey(), s);
							}
						}
					}
				}
				// Receive messages from the current player id
				for (int i = 0; i < numOfProtocols; i++) {
					SCENetworkImpl sceNet = sceNetworks[i];
					Map<Integer, Integer> expectInputFrom = sceNet.getExpectedInputForNextRound();
					if (expectInputFrom.get(pId) != null && expectInputFrom.get(pId) > 0) {
						int numMessages = network.receive(channel, pId);
						Queue<byte[]> messages = new LinkedBlockingQueue<byte[]>();
						for (int j = 0; j < numMessages; j++) {
							byte[] s = network.receive(channel, pId);
							messages.offer(s);
						}
						inputs.get(i).put(pId, messages);
					}
				}
			}
			for (int i = 0; i < numOfProtocols; i++) {
				sceNetworks[i].setInput(inputs.get(i));
				sceNetworks[i].nextRound();
			}
			round++;
		} while (!done);
	}
}
