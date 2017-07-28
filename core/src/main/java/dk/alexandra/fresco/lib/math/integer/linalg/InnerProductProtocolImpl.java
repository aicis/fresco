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
package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class InnerProductProtocolImpl extends AbstractRoundBasedProtocol
    implements InnerProductProtocol {

  private final BasicNumericFactory bnFactory;
  private final EntrywiseProductFactory entrywiseFactory;
  private final SInt[] aVector, bVector;
  private final SInt result;
  private final OInt[] publicBVector;
  private int round = 0;
  private SInt[] results;

  InnerProductProtocolImpl(SInt[] aVector, SInt[] bVector, SInt result,
      BasicNumericFactory bnFactory, EntrywiseProductFactory entrywiseFactory) {
    if (aVector.length != bVector.length) {
      throw new IllegalArgumentException("Lengths of input arrays do not match");
    }
    this.aVector = aVector;
    this.bVector = bVector;
    this.publicBVector = null;
    this.result = result;
    this.bnFactory = bnFactory;
    this.entrywiseFactory = entrywiseFactory;
  }

  InnerProductProtocolImpl(SInt[] aVector, OInt[] bVector, SInt result,
      BasicNumericFactory bnFactory, EntrywiseProductFactory entrywiseFactory) {
    if (aVector.length != bVector.length) {
      throw new IllegalArgumentException("Lengths of input arrays do not match");
    }
    this.aVector = aVector;
    this.bVector = null;
    this.publicBVector = bVector;
    this.result = result;
    this.bnFactory = bnFactory;
    this.entrywiseFactory = entrywiseFactory;
  }

  @Override
  public ProtocolProducer nextProtocolProducer() {
    ProtocolProducer pp;
    if (round == 0) {
      int vectorLength = aVector.length;
      results = new SInt[vectorLength];
      if (vectorLength == 1) {
        round = 2;
        if (publicBVector == null) {
          pp =
              SingleProtocolProducer.wrap(
                  bnFactory.getMultProtocol(aVector[0], bVector[0],
                      result));
        } else {
          pp = SingleProtocolProducer.wrap(
              bnFactory.getMultProtocol(publicBVector[0],
                  aVector[0], result));
        }
        return pp;
      }
      for (int i = 0; i < vectorLength; i++) {
        results[i] = bnFactory.getSInt();
      }
      if (publicBVector != null) {
        pp = entrywiseFactory.getEntrywiseProductProtocol(aVector, publicBVector, results);
      } else {
        pp = entrywiseFactory.getEntrywiseProductProtocol(aVector, bVector, results);
      }
      round++;
    } else if (round == 1) {
      NumericProtocolBuilder build = new NumericProtocolBuilder(bnFactory);
      SInt sumresult = build.sum(results);
      results = null;
      NativeProtocol copy = new CopyProtocolImpl<SInt>(sumresult, result);
      pp = new SequentialProtocolProducer(build.getProtocol(), copy);
      round++;
    } else {
      pp = null;
    }
    return pp;
  }
}