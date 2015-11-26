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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetworkImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public class BatchedStrategy {

	public void processBatch(NativeProtocol[] protocols, int numOfProtocols,
			SCENetworkImpl[] sceNetworks, Network network, String channel,
			ResourcePool rp, Map<Integer, List<Serializable[]>> queues)
			throws IOException {		
		int round = 0;

		boolean[] dones = new boolean[numOfProtocols];
		boolean done;
		// while loop for rounds
		do {
			done = true;
			// For loop for protocols
			for (int i = 0; i < numOfProtocols; i++) {
				SCENetworkImpl sceNetwork = sceNetworks[i];
				if (!dones[i]) {
					EvaluationStatus status = protocols[i].evaluate(round, rp,
							sceNetwork);
					if (status.equals(EvaluationStatus.IS_DONE)) {
						dones[i] = true;
					} else {
						done = false;
					}
				}

				// send phase
				Map<Integer, Queue<Serializable>> output = sceNetwork
						.getOutputFromThisRound();
				for (int pId : output.keySet()) {
					// send array since queue is not serializable
					queues.get(pId).add(
							output.get(pId).toArray(new Serializable[0]));
				}
			}
			// actual send
			for (int pId : queues.keySet()) {
				// send no matter if the double array is empty or not.
				// TODO: Improvement would be not to send something if the queue
				// on pId is empty
				network.send(channel, pId, queues.get(pId)
						.toArray(new Serializable[0][0]));
			}

			// receive phase
			List<Serializable[][]> messages = new ArrayList<Serializable[][]>();
			for (int pId : queues.keySet()) {
				messages.add((Serializable[][]) network.receive(
						channel, pId));
			}
			for (int i = 0; i < numOfProtocols; i++) {
				Map<Integer, Queue<Serializable>> inputForThisRound = new HashMap<Integer, Queue<Serializable>>();
				SCENetworkImpl sceNetwork = sceNetworks[i];
				for (int pId : queues.keySet()) {
					if (sceNetwork.getExpectedInputForNextRound().contains(pId)) {
						inputForThisRound.put(
								pId,
								new LinkedBlockingQueue<Serializable>(Arrays
										.asList(messages.get(pId - 1)[i])));
					}
				}
				sceNetwork.setInput(inputForThisRound);
				sceNetwork.nextRound();
			}

			round++;
			for (int pId = 1; pId <= queues.size(); pId++) {
				queues.get(pId).clear();
			}
		} while (!done);
	}
}
