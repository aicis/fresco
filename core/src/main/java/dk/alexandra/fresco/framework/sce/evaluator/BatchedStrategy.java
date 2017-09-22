/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.SCENetworkSupplier;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
 */
public class BatchedStrategy {

  /**
   * @param protocols array holding the protocols to be evaluated
   * @param sceNetwork array of sceNetworks corresponding to the protocols to be evaluated.
   *     I.e.,
   *     the array should contain numProtocols SCENetworks, with sceNetwork[i] used for
   *     communication in
   *     protocols[i].
   * @param channel string indicating the channel to communicate over.
   * @param rp the resource pool.
   */
  public static <ResourcePoolT extends ResourcePool> void processBatch(
      ProtocolCollection<ResourcePoolT> protocols,
      SCENetwork sceNetwork, int channel, ResourcePoolT rp) throws IOException {
    Network network = rp.getNetwork();
    int round = 0;

    while (protocols.size() > 0) {
      evaluateCurrentRound(protocols, sceNetwork, channel, rp, network, round);

      round++;
    }
  }

  private static <ResourcePoolT extends ResourcePool> void evaluateCurrentRound(
      ProtocolCollection<ResourcePoolT> protocols, SCENetwork sceNetwork,
      int channel, ResourcePoolT rp, Network network, int round) throws IOException {
    Iterator<NativeProtocol<?, ResourcePoolT>> iterator = protocols.iterator();
    while (iterator.hasNext()) {
      NativeProtocol<?, ResourcePoolT> protocol = iterator.next();
      EvaluationStatus status = protocol.evaluate(round, rp, sceNetwork);
      if (status.equals(EvaluationStatus.IS_DONE)) {
        iterator.remove();
      }
    }
    if (sceNetwork instanceof SCENetworkSupplier) {
      // Send/Receive data for this round if SCENetwork is a supplier
      SCENetworkSupplier sceNetworkSupplier = (SCENetworkSupplier) sceNetwork;
      Map<Integer, ByteBuffer> inputs = new HashMap<>();

      //Send data
      Map<Integer, byte[]> output = sceNetworkSupplier.getOutputFromThisRound();
      for (Map.Entry<Integer, byte[]> e : output.entrySet()) {
        network.send(channel, e.getKey(), e.getValue());
      }

      //receive data
      Set<Integer> expected = sceNetworkSupplier.getExpectedInputForNextRound();
      for (int i : expected) {
        byte[] data = network.receive(channel, i);
        inputs.put(i, ByteBuffer.wrap(data));
      }

      sceNetworkSupplier.setInput(inputs);
      sceNetworkSupplier.nextRound();
    }
  }
}