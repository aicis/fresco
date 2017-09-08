/*
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
package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public interface NativeProtocol<OutputT, ResourcePoolT extends ResourcePool> extends
    DRes<OutputT> {

  enum EvaluationStatus {
    IS_DONE, HAS_MORE_ROUNDS
  }

  /**
   * One round of evaluating the gate. Each round consist of only local
   * computation.
   *
   * @param round Number of current round, starting with round 0.
   * @param resourcePool available resources can be found here. This also includes a threadpool if
   * needed. It is advised to use only resources found here instead of creating them yourself. It is
   * strongly advised not to use the network found here, but instead use the protocol network.
   * @param network A protocol's view of the network. This network does not immediately send data,
   * but queues it for later use by the one calling this function.
   * @return True if there are more rounds, i.e., if evaluate needs to be called again, false if
   * this is the last round.
   */
  EvaluationStatus evaluate(int round, ResourcePoolT resourcePool, SCENetwork network);

}
