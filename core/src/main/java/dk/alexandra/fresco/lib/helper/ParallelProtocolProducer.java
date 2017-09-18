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

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A parallel producer contains a set of protocols producer that are asked to fill the collection
 * eagerly. Given some of the producers are lazy initialized this performs best - and hence does a
 * breadth first search for more protocols. THis skews the evalueration of the protocols in favor of
 * the first, but delivers the best performance in terms of memory.
 */
public class ParallelProtocolProducer implements ProtocolProducer, ProtocolProducerCollection {

  private LinkedList<ProtocolProducer> subProducers;

  public ParallelProtocolProducer() {
    subProducers = new LinkedList<>();
  }

  public void append(ProtocolProducer protocolProducer) {
    subProducers.offer(protocolProducer);
  }

  @Override
  public boolean hasNextProtocols() {
    for (Iterator<ProtocolProducer> iterator = subProducers.iterator(); iterator.hasNext(); ) {
      ProtocolProducer producer = iterator.next();
      if (producer.hasNextProtocols()) {
        return true;
      } else {
        iterator.remove();
      }
    }
    return false;
  }

  @Override
  public <ResourcePoolT extends ResourcePool> void getNextProtocols(
      ProtocolCollection<ResourcePoolT> protocolCollection) {
    ListIterator<ProtocolProducer> iterator = subProducers.listIterator();
    while (iterator.hasNext() && protocolCollection.hasFreeCapacity()) {
      ProtocolProducer producer = iterator.next();
      if (producer.hasNextProtocols()) {
        producer.getNextProtocols(protocolCollection);
      } else {
        iterator.remove();
      }
    }
  }

}
