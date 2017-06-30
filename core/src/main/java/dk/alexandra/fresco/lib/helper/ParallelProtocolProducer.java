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

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * If a Parallel protocol has n sub-protocols and is asked to deliver m protocols, it
 * requests the protocols from each of the sub-protocols in a round robin maner.
 */
public class ParallelProtocolProducer implements ProtocolProducer,
    ProtocolProducerCollection {

  private LinkedList<ProtocolProducer> cs;
  private ListIterator<ProtocolProducer> currentIterator;

  public ParallelProtocolProducer() {
    cs = new LinkedList<>();
  }

  public ParallelProtocolProducer(ProtocolProducer... cs) {
    this();
    for (ProtocolProducer c : cs) {
      append(c);
    }
  }

  public ParallelProtocolProducer(ProtocolProducer protocolProducer, Computation... protocols) {
    this();
    append(protocolProducer);
    for (Computation protocol : protocols) {
      append(protocol);
    }
  }

  public ParallelProtocolProducer(Computation... protocols) {
    this();
    for (Computation protocol : protocols) {
      append(protocol);
    }
  }

  public void append(ProtocolProducer protocolProducer) {
    cs.offer(protocolProducer);
  }

  public void append(Computation computation) {
    cs.offer(SingleProtocolProducer.wrap(computation));
  }

  @Override
  public boolean hasNextProtocols() {
    return cs.stream().anyMatch(ProtocolProducer::hasNextProtocols);
  }


  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (cs.isEmpty()) {
      return;
    }
    ProtocolProducer startElement = null;
    if (currentIterator == null) {
      currentIterator = cs.listIterator();
    }
    if (currentIterator.hasNext()) {
      startElement = currentIterator.next();
      startElement.getNextProtocols(protocolCollection);
    }
    addProtocolsFromIterator(protocolCollection, null);
    if (protocolCollection.hasFreeCapacity()) {
      currentIterator = cs.listIterator();
      addProtocolsFromIterator(protocolCollection, startElement);
      if (!currentIterator.hasNext()) {
        currentIterator = null;
      }
    }
  }

  private void addProtocolsFromIterator(ProtocolCollection protocolCollection,
      ProtocolProducer stopElement) {
    while (currentIterator.hasNext() && protocolCollection.hasFreeCapacity()) {
      ProtocolProducer producer = currentIterator.next();
      if (producer == stopElement) {
        return;
      }
      if (producer.hasNextProtocols()) {
        producer.getNextProtocols(protocolCollection);
      } else {
        currentIterator.remove();
      }
    }
  }
}
