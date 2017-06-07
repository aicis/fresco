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

/**
 * A skeleton for a generic round based protocol. I.e., a protocol that consisting
 * of a number of sub-protocols that must be evaluated one after an other. This
 * can be a little more lightweight than using the SequentialprotocolProducer as
 * makes it easier to write a protocol where the sub-protocols are generated only
 * after the previous sub-protocols have been evaluated.
 *
 * @author psn
 */
public abstract class AbstractRoundBasedProtocol implements ProtocolProducer {

  private boolean done = false;
  private ProtocolProducer pp = null;

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (pp == null || !pp.hasNextProtocols()) {
      pp = nextProtocolProducer();
      if (pp == null) {
        done = true;
        return;
      }
    }
    if (pp.hasNextProtocols()) {
      pp.getNextProtocols(protocolCollection);
    }
  }

  /**
   * Gives the sub-protocol for the next round. All protocols of the preceding
   * round are assumed to be evaluated before the next round is evaluated.
   *
   * @return a protocolProducer for the next round.
   */
  public abstract ProtocolProducer nextProtocolProducer();

  @Override
  public boolean hasNextProtocols() {
    return !done;
  }
}
