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
package dk.alexandra.fresco.lib.math.integer.min;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocol;
import dk.alexandra.fresco.lib.compare.ConditionalSelectProtocol;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.SubtractProtocol;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.ProtocolProducerCollection;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPFactory;

public class MinimumProtocolImpl implements MinimumProtocol {

  private SInt[] xs, cs;
  private SInt m;
  private ProtocolProducer currPP;
  private LPFactory lpFactory;
  private BasicNumericFactory numericFactory;
  private final int k;
  private boolean done = false;

  public MinimumProtocolImpl(SInt[] xs, SInt m, SInt[] cs,
      LPFactory lpFactory, BasicNumericFactory numericFactory) {
    if (xs.length == 1) {
      throw new IllegalArgumentException("Minimum protocol. k should never be 1.");
    } else 
    if (xs.length != cs.length) {
      throw new IllegalArgumentException(
          "Min protocol: Output array should be same size as intput array");
    }
    this.k = xs.length;
    this.xs = xs;
    this.cs = cs;
    this.m = m;
    this.lpFactory = lpFactory;
    this.numericFactory = numericFactory;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (currPP == null) {
      if (this.k == 2) {
        SequentialProtocolProducer sequentialProtocolProducer =
            new SequentialProtocolProducer();
        sequentialProtocolProducer.append(
            lpFactory.getComparisonProtocol(
                this.xs[0], this.xs[1], this.cs[0], false));
        sequentialProtocolProducer.append(
            lpFactory.getConditionalSelectProtocol(this.cs[0], this.xs[0], this.xs[1], this.m));
        SInt one = numericFactory.getSInt(1);
        sequentialProtocolProducer.append(
            numericFactory.getSubtractProtocol(one, this.cs[0], this.cs[1]));
        currPP = sequentialProtocolProducer;
      } else if (this.k == 3) {
        SequentialProtocolProducer seqGP = new SequentialProtocolProducer();
        SInt c1_prime = numericFactory.getSInt();
        seqGP.append(
            lpFactory.getComparisonProtocol(
                this.xs[0], this.xs[1], c1_prime, false));
        SInt m1 = numericFactory.getSInt();
        seqGP.append(
            lpFactory
                .getConditionalSelectProtocol(c1_prime, this.xs[0],
                    this.xs[1], m1));

        SInt c2_prime = numericFactory.getSInt();
        seqGP.append(
            lpFactory.getComparisonProtocol(m1,
                this.xs[2], c2_prime, false));
        seqGP.append(
            lpFactory
                .getConditionalSelectProtocol(c2_prime, m1, this.xs[2],
                    this.m));

        seqGP.append(
            numericFactory.getMultProtocol(c1_prime,
                c2_prime, this.cs[0]));
        SubtractProtocol sub1 = numericFactory.getSubtractProtocol(
            c2_prime, this.cs[0], this.cs[1]);
        SInt one = numericFactory.getSInt(1);
        SInt tmp = numericFactory.getSInt();
        SubtractProtocol sub2 = numericFactory.getSubtractProtocol(one,
            this.cs[0], tmp);
        SubtractProtocol sub3 = numericFactory.getSubtractProtocol(tmp,
            this.cs[1], this.cs[2]);

        ParallelProtocolProducer parGP = new ParallelProtocolProducer(sub1,
            sub2);
        currPP = new SequentialProtocolProducer(seqGP, parGP);
        ((ProtocolProducerCollection) currPP).append(sub3);

      } else { // k > 3
        currPP = new RecursionPart();
      }
    }
    if (currPP.hasNextProtocols()) {
      currPP.getNextProtocols(protocolCollection);
    } else {
      currPP = null;
      done = true;
    }
  }

  private class RecursionPart extends AbstractRoundBasedProtocol {

    private int round = 0;
    private SInt m1;
    private SInt m2;
    private SInt[] cs1_prime;
    private SInt[] cs2_prime;

    @Override
    public ProtocolProducer nextProtocolProducer() {
      ProtocolProducer gp = null;
      if (round == 0) {
        int k1 = k / 2;
        int k2 = k - k1;
        SInt[] X1 = new SInt[k1];
        SInt[] X2 = new SInt[k2];
        System.arraycopy(xs, 0, X1, 0, X1.length);
        System.arraycopy(xs, X1.length, X2, 0, X2.length);
        cs1_prime = new SInt[k1];
        cs2_prime = new SInt[k2];
        m1 = numericFactory.getSInt();
        m2 = numericFactory.getSInt();
        System.arraycopy(cs, 0, cs1_prime, 0, k1);
        System.arraycopy(cs, k1, cs2_prime, 0, k2);
        MinimumProtocol min1 = lpFactory.getMinimumProtocol(X1, m1,
            cs1_prime);
        MinimumProtocol min2 = lpFactory.getMinimumProtocol(X2, m2,
            cs2_prime);
        gp = new ParallelProtocolProducer(min1, min2);
        round++;
      } else {
        SInt c = numericFactory.getSInt();
        ComparisonProtocol comp = lpFactory.getComparisonProtocol(m1,
            m2, c, false);
        ConditionalSelectProtocol cond = lpFactory
            .getConditionalSelectProtocol(c, m1, m2, m);
        SInt one = numericFactory.getSInt(1);
        SInt oneMinusC = numericFactory.getSInt();
        SubtractProtocol subtract = numericFactory.getSubtractProtocol(
            one, c, oneMinusC);
        VectorScale scale1 = new VectorScale(c, cs1_prime, cs, 0);
        VectorScale scale2 = new VectorScale(oneMinusC, cs2_prime, cs, k / 2);
        ProtocolProducer scale = new ParallelProtocolProducer(scale1, scale2);
        currPP = new SequentialProtocolProducer(
            comp, cond, SingleProtocolProducer.wrap(subtract), scale);
        round++;
      }
      return gp;
    }
  }

  private class VectorScale extends SimpleProtocolProducer {

    SInt scale;
    SInt[] vector;
    SInt[] output;
    int from;

    VectorScale(SInt scale, SInt[] vector, SInt[] output, int from) {
      this.scale = scale;
      this.vector = vector;
      this.output = output;
      this.from = from;
    }


    @Override
    protected ProtocolProducer initializeProtocolProducer() {
      ParallelProtocolProducer par = new ParallelProtocolProducer();
      for (int i = 0; i < vector.length; i++) {
        NativeProtocol mult = numericFactory.getMultProtocol(scale, vector[i],
            output[from + i]);
        par.append(mult);
      }
      return par;
    }


  }

  @Override
  public boolean hasNextProtocols() {
    return !done;
  }
}
