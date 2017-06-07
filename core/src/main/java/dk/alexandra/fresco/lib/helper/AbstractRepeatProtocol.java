/*******************************************************************************
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
package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;

/**
 * A protocol suited for doing the same simple operation (i.e., operations
 * requiring a very small amount of protocols) many times in parallel.
 *
 * The protocol tries to construct as few protocols as possible. I.e., in each
 * invocation of the getNextprotocols method we try to produce only as many protocols as
 * can fit in protocol array.
 *
 * This could be a more efficient alternative to the ParallelprotocolProducer.
 * because the protocolProducers to be computed in parallel are generated on the
 * fly.
 *
 * @author psn
 */
public abstract class AbstractRepeatProtocol implements ProtocolProducer {

  private boolean done = false;
  private ProtocolProducer current = null;

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (current == null) {
      current = getNextProtocolProducer();
    }
    while (current != null && protocolCollection.hasFreeCapacity()) {
      current.getNextProtocols(protocolCollection);
      if (!current.hasNextProtocols()) {
        current = getNextProtocolProducer();
      }
    }
    if (current == null) {
      done = true;
    }
  }

  /**
   * Generates the next protocolProducer to be evaluated in parallel.
   */
  protected abstract ProtocolProducer getNextProtocolProducer();

  @Override
  public boolean hasNextProtocols() {
    return !done;
  }
}
