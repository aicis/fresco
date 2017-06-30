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
package dk.alexandra.fresco.lib.helper.sequential;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.LazyProtocolProducer;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import java.util.Arrays;
import java.util.LinkedList;

public class SequentialProtocolProducer implements ProtocolProducer, ProtocolProducerCollection {

  private LinkedList<ProtocolProducer> protocolProducers = new LinkedList<>();
  private ProtocolProducer currentProducer;

  public SequentialProtocolProducer(ProtocolProducer... protocolProducers) {
    this.protocolProducers.addAll(Arrays.asList(protocolProducers));
  }

  public SequentialProtocolProducer(Computation... protocols) {
    for (Computation protocol : protocols) {
      append(protocol);
    }
  }

  public SequentialProtocolProducer(ProtocolProducer firstProtocolProducer,
      Computation... protocols) {
    append(firstProtocolProducer);
    for (Computation protocol : protocols) {
      append(protocol);
    }
  }

  public SequentialProtocolProducer() {

  }

  public void append(ProtocolProducer protocolProducer) {
    this.protocolProducers.add(protocolProducer);
  }

  public void append(Computation computation) {
    this.protocolProducers.add(SingleProtocolProducer.wrap(computation));
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (currentProducer == null) {
      currentProducer = protocolProducers.getFirst();
      if (currentProducer instanceof LazyProtocolProducer) {
        LazyProtocolProducer currentProducer = (LazyProtocolProducer) this.currentProducer;
        currentProducer.checkReady();
        this.currentProducer = currentProducer.protocolProducer;
      }
    }
    protocolProducers.getFirst().getNextProtocols(protocolCollection);
  }

  @Override
  public boolean hasNextProtocols() {
    while (!protocolProducers.isEmpty() && !protocolProducers.getFirst().hasNextProtocols()) {
      protocolProducers.removeFirst();
      currentProducer = null;
    }
    return !protocolProducers.isEmpty();
  }

  @Override
  public String toString() {
    return "SequentialProtocolProducer{"
        + ", protocolProducers=" + protocolProducers
        + '}';
  }
}
