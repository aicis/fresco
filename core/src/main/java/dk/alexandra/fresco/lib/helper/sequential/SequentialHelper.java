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
package dk.alexandra.fresco.lib.helper.sequential;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;

/**
 * This implementation is lazy in the sense that it only invokes hasMoreprotocols()
 * and getNextprotocols() on a protocol when needed.
 **/
class SequentialHelper implements ProtocolProducer {

  private ProtocolProducerList producerList;
  private ProtocolProducer currentProducer = null;
  private boolean log;
  private Class<?> aClass;

  SequentialHelper(ProtocolProducerList cl) {
    this.producerList = cl;
  }

  /*
   * If prune returns false, we are done and no more protocols can be produced.
   * If prune returns true, currentprotocol is a protocol with protocols to evaluate.
   */
  private boolean prune() {
    // Bootstrapping (for when currentprotocol is initially null)
    if (currentProducer == null) {
      if (producerList.hasNextInLine()) {
        currentProducer = producerList.getNextInLine();
      } else {
        return false;
      }
    }
    if (log && currentProducer.hasNextProtocols()) {
      log("Reiterating on " + currentProducer);
    }
    // Roll until a protocol has protocols or the end is reached
    while (!currentProducer.hasNextProtocols()) {
      if (producerList.hasNextInLine()) {
        currentProducer = producerList.getNextInLine();
        if (log) {
          log("Iterating on " + currentProducer);
        }
      } else {
        /* NOTE: we could set currentprotocol to null here
         * to release it to the GC - it would not break the method
				 */
        currentProducer = null;
        if (log) {
          log("Done iterating");
        }
        return false;
      }
    }
    return true;
  }

  private void log(String x) {
    System.out.println(aClass.getSimpleName() + " " + x);
  }

  @Override
  public boolean hasNextProtocols() {
    return prune();
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {

    if (prune()) {
      currentProducer.getNextProtocols(protocolCollection);
    }
  }

  @Override
  public String toString() {
    return "SequentialHelper{"
        + "currentProducer=" + currentProducer
        + '}';
  }

  public void logProgess(Class<?> aClass) {
    this.aClass = aClass;
    log = true;
  }
}
