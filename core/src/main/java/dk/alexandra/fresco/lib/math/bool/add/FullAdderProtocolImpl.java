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
package dk.alexandra.fresco.lib.math.bool.add;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;

/**
 * This class implements a Full Adder protocol for Binary protocols.
 * It takes the naive approach of linking 1-Bit-Full Adders together to implement
 * a generic length adder.
 *
 * @author Kasper Damgaard
 */
public class FullAdderProtocolImpl implements FullAdderProtocol {

  private SBool[] lefts, rights, outs;
  private SBool inCarry, outCarry;
  private SBool tmpCarry;
  private OneBitFullAdderProtocolFactory FAFactory;
  private int round;
  private int stopRound;
  private ProtocolProducer curPP;

  public FullAdderProtocolImpl(SBool[] lefts, SBool[] rights, SBool inCarry, SBool[] outs,
      SBool outCarry, BasicLogicFactory basicFactory, OneBitFullAdderProtocolFactory FAFactory) {
    if (lefts.length != rights.length || lefts.length != outs.length) {
      throw new IllegalArgumentException("input and output arrays for Full Adder must be of same length.");
    }
    this.lefts = lefts;
    this.rights = rights;
    this.inCarry = inCarry;
    this.outs = outs;
    this.outCarry = outCarry;
    this.FAFactory = FAFactory;
    this.round = 0;
    this.stopRound = lefts.length;
    this.curPP = null;

    tmpCarry = basicFactory.getSBool();
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (round == 0) {
      if (curPP == null) {
        curPP = FAFactory
            .getOneBitFullAdderProtocol(lefts[stopRound - 1], rights[stopRound - 1], inCarry,
                outs[stopRound - 1], tmpCarry);
      }
    } else if (round < stopRound - 1) {
      if (curPP == null) {
        //TODO: Using tmpCarry both as in and out might not be good for all implementations of a 1Bit FA protocol?
        //But at least it works for OneBitFullAdderprotocolImpl.
        curPP = FAFactory
            .getOneBitFullAdderProtocol(lefts[stopRound - round - 1], rights[stopRound - round - 1],
                tmpCarry, outs[stopRound - round - 1], tmpCarry);
      }
    } else {
      if (curPP == null) {
        curPP = FAFactory
            .getOneBitFullAdderProtocol(lefts[0], rights[0], tmpCarry, outs[0], outCarry);
      }
    }
    if (curPP.hasNextProtocols()) {
      curPP.getNextProtocols(protocolCollection);
    } else {
      round++;
      curPP = null;
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return round < stopRound;
  }

}
