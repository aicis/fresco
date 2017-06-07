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
package dk.alexandra.fresco.lib.helper.bristol;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;

/**
 * A circuit that is based on Bristol circuits, i.e., it reads a textual
 * description of a circuit in Bristol format (see
 * https://www.cs.bris.ac.uk/Research/CryptographySecurity/MPC/) using the
 * BristolCircuitParser.
 */
public class BristolCircuit implements ProtocolProducer {

  private BristolCircuitParser parser;

  private int pos = 0;

  public BristolCircuit(BristolCircuitParser parser) {
    this.parser = parser;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    // TODO: This is a bit hacky.
    // It only works if the BasicLogicFactory given to CircuitParser
    // produces circuits for AND, XOR, NOT that are native protocols.
    // Otherwise, more book-keeping is needed here.
    ProtocolProducer[] c = new ProtocolProducer[1];
    while (protocolCollection.hasFreeCapacity()) {
      int resCircuit = this.parser.getNext(c, 0);
      if (resCircuit == 0) {
        // The next circuit has input that depends on some unReady input, or end of circuit reached. End batch.
        break;
      } else if (resCircuit != 1) {
        throw new MPCException("Weird, should give one circuit exactly, pos: " + resCircuit);
      }

      // Got exactly one circuit.
      ProtocolProducer protocolProducer = c[0];
      if (!protocolProducer.hasNextProtocols()) {
        return;
      }
      protocolProducer.getNextProtocols(protocolCollection);
      this.pos++;
    }
    //System.out.println("Returning protocols; internal pos: " + this.pos);
  }

  @Override
  public boolean hasNextProtocols() {
    return this.pos < this.parser.getNoOfGates();
  }
}
