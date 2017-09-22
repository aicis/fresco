/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.network.SCENetworkSupplier;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOutputProtocol;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpdzBatchedStrategy {

  /**
   * Process output protocols in the same manner as the
   * {@link dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy} does. The only change is
   * that the round argument starts at 1 which will trigger the SpdzOutput protocols to actually
   * evaluate, instead of just waiting.
   * 
   * @param protocols The output protocols to evaluate
   * @param sceNetwork The SCE internal network to use
   * @param rp The resource pool used
   * @throws IOException
   */
  public static void processOutputProtocolBatch(List<SpdzOutputProtocol<?>> protocols,
      SCENetwork sceNetwork, SpdzResourcePool rp) throws IOException {
    Network network = rp.getNetwork();
    int round = 1;

    while (protocols.size() > 0) {
      evaluateCurrentRound(protocols, sceNetwork, 0, rp, network, round);

      round++;
    }
  }

  private static void evaluateCurrentRound(List<SpdzOutputProtocol<?>> protocols,
      SCENetwork sceNetwork, int channel, SpdzResourcePool rp, Network network, int round)
          throws IOException {
    Iterator<SpdzOutputProtocol<?>> iterator = protocols.iterator();
    while (iterator.hasNext()) {
      NativeProtocol<?, SpdzResourcePool> protocol = iterator.next();
      EvaluationStatus status = protocol.evaluate(round, rp, sceNetwork);
      if (status.equals(EvaluationStatus.IS_DONE)) {
        iterator.remove();
      }
    }
    if (sceNetwork instanceof SCENetworkSupplier) {
      // Send/Receive data for this round if SCENetwork is a supplier
      SCENetworkSupplier sceNetworkSupplier = (SCENetworkSupplier) sceNetwork;
      Map<Integer, ByteBuffer> inputs = new HashMap<>();

      // Send data
      Map<Integer, byte[]> output = sceNetworkSupplier.getOutputFromThisRound();
      for (Map.Entry<Integer, byte[]> e : output.entrySet()) {
        network.send(channel, e.getKey(), e.getValue());
      }

      // receive data
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
