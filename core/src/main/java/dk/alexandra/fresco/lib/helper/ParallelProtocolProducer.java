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
package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * If a Parallel protocol has n sub-protocols and is asked to deliver m protocols, it
 * requests m/n protocols from each of the sub-protocols.
 */
public class ParallelProtocolProducer implements ProtocolProducer,
    ProtocolProducerCollection {

  private LinkedList<ProtocolProducer> cs;

  public ParallelProtocolProducer() {
    cs = new LinkedList<>();
  }

  public ParallelProtocolProducer(ProtocolProducer... cs) {
    this();
    for (ProtocolProducer c : cs) {
      append(c);
    }
  }

  public ParallelProtocolProducer(ProtocolProducer protocolProducer, NativeProtocol... protocols) {
    this();
    append(protocolProducer);
    for (NativeProtocol protocol : protocols) {
      append(protocol);
    }
  }

  public ParallelProtocolProducer(NativeProtocol... protocols) {
    this();
    for (NativeProtocol protocol : protocols) {
      append(protocol);
    }
  }

  public void append(ProtocolProducer protocolProducer) {
    cs.offer(protocolProducer);
  }

  public void append(NativeProtocol computation) {
    cs.offer(SingleProtocolProducer.wrap(computation));
  }

  @Override
  public boolean hasNextProtocols() {
    prune();
    return !cs.isEmpty();
  }

  /**
   * Removes any empty protocols.
   */
  private void prune() {
    while (!cs.isEmpty()) {
      if (cs.getFirst().hasNextProtocols()) {
        return;
      } else {
        cs.remove();
      }
    }
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    // TODO: This is a simple, but very rough implementation.
    // It requests an equal amount from each subprotocol and only asks once.
    // A better implementation should try to fill up the protocol array by
    // requesting further protocols from large protocols if the smaller protocols
    // run dry.
    // E.g. this implementation is inferior in that it may return less protocols
    // than it could.
    if (cs.size() == 0) {
      return;
    }
    ListIterator<ProtocolProducer> x = cs.listIterator();
    while (x.hasNext()) {
      ProtocolProducer c = x.next();
      if (!c.hasNextProtocols()) {
        x.remove();
      } else {
        c.getNextProtocols(protocolCollection);
      }
      if (!protocolCollection.hasFreeCapacity()) {
        return; // We've filled the array.
      }
    }
  }

}
