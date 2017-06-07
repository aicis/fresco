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
package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.compare.KeyedCompareAndSwapProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.BasicLogicBuilder;
import dk.alexandra.fresco.lib.logic.AbstractBinaryFactory;

public class KeyedCompareAndSwapProtocolGetNextProtocolImpl extends SimpleProtocolProducer
    implements KeyedCompareAndSwapProtocol {

  private SBool[] leftKey;
  private SBool[] leftValue;
  private SBool[] rightKey;
  private SBool[] rightValue;
  private AbstractBinaryFactory bf;
  private ProtocolProducer curPP = null;
  private boolean done = false;
  private int round;

  private SBool compRes;
  private SBool[] tmpXORKey;
  private SBool[] tmpXORValue;

  /**
   * Constructs a protocol producer for the keyed compare and swap protocol. This
   * protocol will compare the keys of two key-value pairs and swap the pairs
   * so that the left pair has the largest key.
   *
   * @param leftKey the key of the left pair
   * @param leftValue the value of the left pair
   * @param rightKey the key of the right pair
   * @param rightValue the value of the right pair
   * @param bf a factory of binary protocols
   */
  public KeyedCompareAndSwapProtocolGetNextProtocolImpl(SBool[] leftKey, SBool[] leftValue,
      SBool[] rightKey, SBool[] rightValue, AbstractBinaryFactory bf) {
    this.leftKey = leftKey;
    this.leftValue = leftValue;
    this.rightKey = rightKey;
    this.rightValue = rightValue;
    this.bf = bf;
    this.round = 0;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (round == 0) {
      if (curPP == null) {
        curPP = new ParallelProtocolProducer();

        compRes = bf.getSBool();
        ((ParallelProtocolProducer) curPP)
            .append(bf.getBinaryComparisonProtocol(leftKey, rightKey, compRes));

        tmpXORKey = new SBool[leftKey.length];
        for (int i = 0; i < leftKey.length; i++) {
          tmpXORKey[i] = bf.getSBool();
          ((ParallelProtocolProducer) curPP)
              .append(bf.getXorProtocol(leftKey[i], rightKey[i], tmpXORKey[i]));
        }

        tmpXORValue = new SBool[leftValue.length];
        for (int i = 0; i < leftValue.length; i++) {
          tmpXORValue[i] = bf.getSBool();
          ((ParallelProtocolProducer) curPP)
              .append(bf.getXorProtocol(leftValue[i], rightValue[i], tmpXORValue[i]));
        }
      }
      getNextFromCurPp(protocolCollection, false);
    } else if (round == 1) {
      if (curPP == null) {
        BasicLogicBuilder blb = new BasicLogicBuilder(bf);
        blb.beginParScope();
        blb.condSelectInPlace(leftKey, compRes, leftKey, rightKey);
        blb.condSelectInPlace(leftValue, compRes, leftValue, rightValue);
        blb.endCurScope();
        curPP = blb.getProtocol();
      }
      getNextFromCurPp(protocolCollection, false);
    } else if (round == 2) {
      if (curPP == null) {
        curPP = new ParallelProtocolProducer();
        for (int i = 0; i < rightKey.length; i++) {
          ((ParallelProtocolProducer) curPP)
              .append(bf.getXorProtocol(tmpXORKey[i], leftKey[i], rightKey[i]));
        }
        for (int i = 0; i < rightValue.length; i++) {
          ((ParallelProtocolProducer) curPP)
              .append(bf.getXorProtocol(tmpXORValue[i], leftValue[i], rightValue[i]));
        }
      }
      getNextFromCurPp(protocolCollection, true);
    }
  }

  private void getNextFromCurPp(ProtocolCollection protocolCollection, boolean done) {
    if (curPP.hasNextProtocols()) {
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

  @Override
  protected ProtocolProducer initializeProtocolProducer() {
    return this;
  }


}