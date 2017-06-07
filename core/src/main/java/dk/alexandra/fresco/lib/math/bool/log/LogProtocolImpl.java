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
package dk.alexandra.fresco.lib.math.bool.log;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Protocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.math.Util;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.XorProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.OrFromXorAndProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

/**
 * This class implements logarithm base 2 for binary protocols. It is currently up to the
 * application programmer to check if the input is 0. It is well-defined to input 0 and will return
 * 0, but this is not correct as log_2(0) = NaN It uses a method consisting of 3 steps: - Prefix OR:
 * Starting from most significant bit of the input X, OR with the next bit. This causes a bit vector
 * of the form Y=[0,0,...,1,1,...,1]. - XOR sum: The resulting bit Zi in the bit vector Z is given
 * as: Y(i+1) XOR Yi This gives a result of the form: Z=[0,...,0,1,0,...,0]. This is the result of
 * the function 2^{floor(log(X))+1}. - Finally, we get hold of only floor(log(X))+1 by having the
 * result Ai become: forall j: XOR (Zj AND i'th bit of j) This means fx if Z = [0,1,0], then A0
 * becomes = (Z0 AND 0'th bit of 0) XOR (Z1 AND 0'th bit of 1) XOR (Z2 AND 0'th bit of 2) = 0 XOR 1
 * XOR 0 = 1 Whereas A1 = (Z0 AND 1'th bit of 0) XOR (Z1 AND 1'th bit of 1) XOR (Z2 AND 1'th bit of
 * 2) = 0 XOR 0 XOR 0 = 0 and A2 is also 0, which gives the correct result of A = [0,0,1].
 *
 * @author Kasper Damgaard
 */
public class LogProtocolImpl implements LogProtocol {

  private SBool[] number, result;
  private SBool[] prefixOrOuts; //y
  private SBool[] log, xorHolders, tmps;
  private BasicLogicFactory factory;
  private ProtocolProducer curPP;
  private int round;

  /**
   * Note that on an input of 0, this implementation yields 0, which is incorrect.
   * The application is itself responsible for checking that we do indeed not input 0.
   *
   * @param number The number which we want to calculate log base 2 on.
   * @param result Placeholder for the result of the computation.
   * @param factory A Basic Logic factory
   */
  public LogProtocolImpl(SBool[] number, SBool[] result, BasicLogicFactory factory) {
    if (result.length != Util.log2(number.length) + 1) {
      throw new MPCException("Output array must be log size+1 that of the input array!");
    }
    this.number = number;
    this.result = result;
    this.factory = factory;
    this.curPP = null;
    this.round = 0;

    prefixOrOuts = new SBool[number.length];
    for (int i = 0; i < number.length; i++) {
      prefixOrOuts[i] = factory.getSBool();
    }

    log = new SBool[number.length + 1];
    tmps = new SBool[log.length];
    for (int i = 0; i < log.length; i++) {
      log[i] = factory.getSBool();
      tmps[i] = factory.getSBool();
    }
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (round == 0) {
      if (curPP == null) {
        ProtocolProducer[] prefixOrs = new ProtocolProducer[number.length - 1];
        prefixOrOuts[0] = number[0];
        for (int i = 1; i < number.length; i++) {
          ProtocolProducer or = new OrFromXorAndProtocol(factory, factory, factory, number[i],
              prefixOrOuts[i - 1], prefixOrOuts[i]);
          prefixOrs[i - 1] = or;
        }
        curPP = new SequentialProtocolProducer(prefixOrs);
      }
      getNextFromCur(protocolCollection);
    } else if (round == 1) {
      if (curPP == null) {
        Protocol[] xors = new Protocol[number.length + 1];
        for (int i = xors.length - 2; i > -1; i--) {
          if (i == 0) {
            OBool zero = factory.getKnownConstantOBool(
                false); //get a 0 which we implicitly prepends to y (prefixOrOuts)
            xors[i] = factory.getXorProtocol(prefixOrOuts[i], zero, log[i]);
          } else {
            xors[i] = factory.getXorProtocol(prefixOrOuts[i - 1], prefixOrOuts[i], log[i]);
          }
        }
        xors[xors.length - 1] = factory.getXorProtocol(prefixOrOuts[0], prefixOrOuts[0],
            log[xors.length - 1]); //This is the same as saying that a 0 should
        //always be at the least significant bit position at z
        curPP = new ParallelProtocolProducer(xors);
      }
      getNextFromCur(protocolCollection);
    } else if (round == 2) {
      if (curPP == null) {
        curPP = new ParallelProtocolProducer();
        for (int j = 0; j < result.length; j++) {
          xorHolders = new SBool[log.length];
          XorProtocol preResult = factory.getXorProtocol(number[0], number[0],
              result[j]); //"hack" in order to make result be 0 from starting point.

          SequentialProtocolProducer ands = new SequentialProtocolProducer();
          SequentialProtocolProducer xors = new SequentialProtocolProducer(preResult);

          for (int i = 0; i < log.length; i++) {
            xorHolders[i] = factory.getSBool();
            boolean ithBit = Util
                .ithBit(log.length - 1 - i, result.length - 1 - j); //j'th bit of i
            ands.append(factory
                .getAndProtocol(log[i], factory.getKnownConstantOBool(ithBit), xorHolders[i]));
            xors.append(factory.getXorProtocol(xorHolders[i], result[j], result[j]));
          }

          ((ParallelProtocolProducer) curPP).append(ands);
          ((ParallelProtocolProducer) curPP).append(xors);
        }
      }
      getNextFromCur(protocolCollection);
    }
  }

  private void getNextFromCur(ProtocolCollection protocolCollection) {
    if (curPP.hasNextProtocols()) {
      curPP.getNextProtocols(protocolCollection);
    } else {
      round++;
      curPP = null;
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return round < 3;
  }
}
