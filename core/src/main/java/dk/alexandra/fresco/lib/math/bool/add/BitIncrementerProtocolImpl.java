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
 * @author Michael Stausholm
 */
public class BitIncrementerProtocolImpl implements BitIncrementerProtocol {

  private SBool[] base, outs;
  private SBool increment;
  private SBool tmpCarry;
  private OneBitHalfAdderProtocolFactory FAFactory;
  private int round;
  private int stopRound;
  private ProtocolProducer curPP;
  private BasicLogicFactory basicFactory;

  public BitIncrementerProtocolImpl(SBool[] base, SBool increment, SBool[] outs,
      BasicLogicFactory basicFactory, OneBitHalfAdderProtocolFactory FAFactory) {
    /*if(base.length != (outs.length-1)){
      throw new MPCException("output must be 1 larger than input.");
		}*/
    this.base = base;

    this.increment = increment;
    this.outs = outs;
    this.FAFactory = FAFactory;
    this.round = 0;
    this.stopRound = base.length;
    this.curPP = null;

    this.basicFactory = basicFactory;
    tmpCarry = basicFactory.getSBool();
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (round == 0) {
      if (curPP == null) {
        curPP = FAFactory
            .getOneBitHalfAdderProtocol(base[stopRound - 1], increment, outs[stopRound - 1],
                tmpCarry);
      }
    } else if (round > 0 && round < stopRound - 1) {
      //System.out.println("Got to round "+round);
      if (curPP == null) {
        //TODO: Using tmpCarry both as in and out might not be good for all implementations of a 1Bit FA protocol?
        //But at least it works for OneBitFullAdderprotocolImpl.
        curPP = FAFactory
            .getOneBitHalfAdderProtocol(base[stopRound - round - 1], tmpCarry,
                outs[stopRound - round - 1], tmpCarry);
      }
    } else if (round == stopRound - 1) {
      //System.out.println("Got to final round");
      if (curPP == null) {
        SBool last = basicFactory.getKnownConstantSBool(false);
        if (outs.length > base.length) {
          last = outs[outs.length - base.length - 1];
        }

        curPP = FAFactory
            .getOneBitHalfAdderProtocol(base[0], tmpCarry, outs[outs.length - base.length], last);
      }
    }
    getNextFromCurPp(protocolCollection);
  }

  private void getNextFromCurPp(ProtocolCollection protocolCollection) {
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
