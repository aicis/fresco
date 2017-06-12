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
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ConditionalSelectProtocol;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.inv.InversionProtocol;

public class UpdateMatrixProtocol implements ProtocolProducer {

  private Matrix<SInt> oldUpdateMatrix;
  private Matrix<SInt> newUpdateMatrix;
  private SInt[] L, C;
  private SInt p, p_prime;
  private LPFactory lpFactory;
  private BasicNumericFactory numericFactory;
  private ProtocolProducer curPP;
  private boolean done = false;

  UpdateMatrixProtocol(Matrix<SInt> oldUpdateMatrix, SInt[] L, SInt[] C, SInt p,
      SInt p_prime, Matrix<SInt> newUpdateMatrix,
      LPFactory lpFactory, BasicNumericFactory numericFactory) {
    this.oldUpdateMatrix = oldUpdateMatrix;
    this.newUpdateMatrix = newUpdateMatrix;
    this.L = L;
    this.C = C;
    this.p = p;
    this.p_prime = p_prime;
    this.lpFactory = lpFactory;
    this.numericFactory = numericFactory;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (curPP == null) {
      SInt p_prime_inv, pp, one;
      p_prime_inv = numericFactory.getSInt();
      pp = numericFactory.getSInt();
      one = numericFactory.getSInt(1);

      InversionProtocol inv = lpFactory.getInversionProtocol(p_prime, p_prime_inv);
      Computation<? extends SInt> mult1 = numericFactory.getMultProtocol(p, p_prime_inv, pp);

      int h = oldUpdateMatrix.getHeight();
      int w = oldUpdateMatrix.getWidth();
      // These 3 for the generation of lambda_i's
      SInt[][] lambdas_i_jOuts = new SInt[h][w];
      SInt[] lambdas_iOuts = new SInt[h]; // same as [v'_i,L]
      Computation[][] mults_l_v = new Computation[h - 1][w];
      Computation[][] addsLambda_i = new Computation[h][w];

      // next 4 for the update equation
      SInt[][] subOuts = new SInt[h][w];
      Computation[][] subs = new Computation[h][w];
      SInt[][] mults_cAndLambda_iOuts = new SInt[h][w];
      Computation[][] mults_cAndLambda_i = new Computation[h][w];
      SInt[][] mults_sub_and_ppOuts = new SInt[h][w];
      Computation[][] mults_sub_and_pp = new Computation[h][w];
      Computation[][] adds = new Computation[h][w];

      // This one divides C by the previous pivot
      ProtocolProducer[] scales_c = new ProtocolProducer[C.length];
      SInt[] scalings = new SInt[C.length];
      for (int j = 0; j < C.length - 1; j++) {
        scalings[j] = numericFactory.getSInt();
        ConditionalSelectProtocol scaling = lpFactory
            .getConditionalSelectProtocol(L[j], one, p_prime_inv, scalings[j]);
        Computation<? extends SInt> divides_c_by_p_prime = numericFactory
            .getMultProtocol(C[j], scalings[j], C[j]);
        scales_c[j] = new SequentialProtocolProducer(scaling, divides_c_by_p_prime);
      }
      scales_c[C.length - 1] =
          SingleProtocolProducer.wrap(
              numericFactory.getMultProtocol(C[C.length - 1], p_prime_inv, C[C.length - 1]));
      ProtocolProducer gpDivideC = new ParallelProtocolProducer(scales_c);

      for (int i = 0; i < oldUpdateMatrix.getHeight(); i++) {
        lambdas_iOuts[i] = numericFactory.getSInt(0);
        for (int j = 0; j < oldUpdateMatrix.getWidth(); j++) {
          //first 3 - lambda_i's generation
          lambdas_i_jOuts[j][i] = numericFactory.getSInt();
          if (j < oldUpdateMatrix.getWidth() - 1) {
            mults_l_v[j][i] = numericFactory
                .getMultProtocol(L[j], oldUpdateMatrix.getElement(j, i), lambdas_i_jOuts[j][i]);
          } else {
            lambdas_i_jOuts[j][i] = numericFactory.getSInt(0);
          }
          //TODO: Check that we add the correct amount. (from j=1 to m+1)
          addsLambda_i[j][i] = numericFactory
              .getAddProtocol(lambdas_i_jOuts[j][i], lambdas_iOuts[i], lambdas_iOuts[i]);

          //next 4 - update matrix
          subOuts[j][i] = numericFactory.getSInt();
          subs[j][i] = numericFactory
              .getSubtractProtocol(oldUpdateMatrix.getElement(j, i), lambdas_i_jOuts[j][i],
                  subOuts[j][i]);
          mults_cAndLambda_iOuts[j][i] = numericFactory.getSInt();
          mults_cAndLambda_i[j][i] = numericFactory
              .getMultProtocol(C[j], lambdas_iOuts[i], mults_cAndLambda_iOuts[j][i]);
          mults_sub_and_ppOuts[j][i] = numericFactory.getSInt();
          mults_sub_and_pp[j][i] = numericFactory
              .getMultProtocol(subOuts[j][i], pp, mults_sub_and_ppOuts[j][i]);
          adds[j][i] = numericFactory
              .getAddProtocol(mults_cAndLambda_iOuts[j][i], mults_sub_and_ppOuts[j][i],
                  newUpdateMatrix.getIthRow(j)[i]);
        }
      }

      SequentialProtocolProducer seq_pp = new SequentialProtocolProducer(inv, mult1);
      seq_pp.append(gpDivideC);

      ProtocolProducer gpMultLambda = getParallelGP(mults_l_v);
      ParallelProtocolProducer par1 = new ParallelProtocolProducer(seq_pp, gpMultLambda);
      // This stuff may not be safe
      //GateProducer gpAddsLambda = getParallelGP(addsLambda_i);
      ParallelProtocolProducer gpAddsLambda = new ParallelProtocolProducer();
      for (int i = 0; i < addsLambda_i[0].length; i++) {
        SequentialProtocolProducer seqAdd = new SequentialProtocolProducer();
        for (int j = 0; j < addsLambda_i.length; j++) {
          seqAdd.append(addsLambda_i[j][i]);
        }
        gpAddsLambda.append(seqAdd);
      }

      ProtocolProducer gpSub = getParallelGP(subs);
      ParallelProtocolProducer gpAddAndSub = new ParallelProtocolProducer(gpAddsLambda, gpSub);
      SequentialProtocolProducer seq2 = new SequentialProtocolProducer(par1, gpAddAndSub);
      ProtocolProducer gpMultCAndLambda = getParallelGP(mults_cAndLambda_i);
      ProtocolProducer gpMultSubAndPP = getParallelGP(mults_sub_and_pp);
      ProtocolProducer gpAdds = getParallelGP(adds);

      ParallelProtocolProducer gpMults = new ParallelProtocolProducer(gpMultCAndLambda,
          gpMultSubAndPP);

      curPP = new SequentialProtocolProducer(seq2, gpMults, gpAdds);
    }
    if (curPP.hasNextProtocols()) {
      curPP.getNextProtocols(protocolCollection);
    } else {
      curPP = null;
      done = true;
    }
  }

  private ParallelProtocolProducer getParallelGP(Computation[][] c) {
    ParallelProtocolProducer[] gps = new ParallelProtocolProducer[c.length];
    for (int i = 0; i < c.length; i++) {
      gps[i] = new ParallelProtocolProducer(c[i]);
    }
    return new ParallelProtocolProducer(gps);
  }

  @Override
  public boolean hasNextProtocols() {
    return !done;
  }

}
