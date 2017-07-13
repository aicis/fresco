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
package dk.alexandra.fresco.lib.compare.bool;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

/**
 * Represents a comparison protocol between two bitstrings. Concretely, the
 * protocol computes the 'greater than' relation of strings A and B, i.e., it
 * computes C := A > B.
 *
 * This uses the method of GenericBinaryComparison2 but is implemented a lot
 * cleaner and fixes some bugs.
 *
 * @author psn
 */
public class BinaryGreaterThanNextProtocolsImpl implements BinaryGreaterThanProtocol {

  private SBool[] inA;
  private SBool[] inB;
  private SBool outC;

  private SBool[] postfixResult;

  private AbstractBinaryFactory factory;

  private int length;

  private boolean done;
  private int round;
  private ProtocolProducer curPP;
  private SBool[] xor;

  /**
   * Construct a protocol to compare strings A and B. The bitstrings A and B
   * are assumed to be even length and to be ordered from most- to least
   * significant bit.
   *
   * @param inA input string A
   * @param inB input string B
   * @param outC a bit to hold the output C := A > B.
   * @param factory a protocol factory
   */
  public BinaryGreaterThanNextProtocolsImpl(SBool[] inA, SBool[] inB, SBool outC,
      AbstractBinaryFactory factory) {
    //if (inA.length == inB.length) { //Check should be performed in 
    // calling class (eg. BasicLogicBuilder)
      this.factory = factory;
      this.outC = outC;
      this.inA = inA;
      this.inB = inB;
      this.length = inA.length;
    //} else {
    //  throw new RuntimeException("Comparison failed: bitsize differs");
    //}
    round = 0;
    postfixResult = new SBool[this.length];
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (round == 0) {
      if (curPP == null) {
        curPP = new ParallelProtocolProducer();
        //		BasicLogicBuilder blb = new BasicLogicBuilder(provider);
        //		xor = blb.xor(inB, inA);
        //		curGP = blb.getprotocol();
        xor = new SBool[inB.length];
        for (int i = 0; i < inB.length; i++) {
          xor[i] = factory.getKnownConstantSBool(false);
          ((ParallelProtocolProducer) curPP).append(factory.getXorProtocol(inB[i], inA[i], xor[i]));
        }
      }
      getNextFromCurrent(protocolCollection, false, curPP);
    } else if (round == 1) {
      if (curPP == null) {
        postfixResult[length - 1] = factory.getSBool();
        curPP = factory.getAndProtocol(inA[length - 1], xor[length - 1], postfixResult[length - 1]);
      }
      getNextFromCurrent(protocolCollection, false, curPP);
    } else if (round <= length) {
      if (curPP == null) {
        curPP = new SequentialProtocolProducer();
        //BasicLogicBuilder blb = new BasicLogicBuilder(provider);
        //blb.beginSeqScope();
        int i = length - round;
        SBool tmp = factory.getSBool();
        //postfixResult[i+1] = provider.getKnownConstantSBool(false);
        postfixResult[i] = factory.getSBool();
        ((SequentialProtocolProducer) curPP)
            .append(factory.getXorProtocol(inA[i], postfixResult[i + 1], tmp));
        //SBool tmp = blb.xor(inA[i], postfixResult[i + 1]);
        ((SequentialProtocolProducer) curPP).append(factory.getAndProtocol(xor[i], tmp, tmp));
        //tmp = blb.and(xor[i], tmp);
        //postfixResult[i] = blb.xor(tmp, postfixResult[i + 1]);
        ((SequentialProtocolProducer) curPP)
            .append(factory.getXorProtocol(tmp, postfixResult[i + 1], postfixResult[i]));
        //	blb.endCurScope();
        //curGP = blb.getCircuit();
      }
      getNextFromCurrent(protocolCollection, false, curPP);
    } else {
      if (curPP == null) {
        curPP = SingleProtocolProducer.wrap(factory.getCopyProtocol(postfixResult[0], outC));
      }
      getNextFromCurrent(protocolCollection, true, curPP);
    }
  }

  private void getNextFromCurrent(
      ProtocolCollection protocolCollection, boolean done, ProtocolProducer currentPP) {
    if (currentPP.hasNextProtocols()) {
      curPP.getNextProtocols(protocolCollection);
    } else {
      round++;
      curPP = null;
      this.done = done;
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return !done;
  }
}
