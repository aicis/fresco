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
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ConditionalSelect;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class UpdateMatrixProtocol4 implements ComputationBuilder<Matrix4<Computation<SInt>>> {

  private Matrix4<Computation<SInt>> oldUpdateMatrix;
  private List<Computation<SInt>> L;
  private List<Computation<SInt>> C;
  private Computation<SInt> p, p_prime;

  UpdateMatrixProtocol4(Matrix4<Computation<SInt>> oldUpdateMatrix, List<Computation<SInt>> L,
      List<Computation<SInt>> C, Computation<SInt> p,
      Computation<SInt> p_prime) {
    this.oldUpdateMatrix = oldUpdateMatrix;
    this.L = L;
    this.C = C;
    this.p = p;
    this.p_prime = p_prime;
  }


  @Override
  public Computation<Matrix4<Computation<SInt>>> build(SequentialProtocolBuilder builder) {
    int height = oldUpdateMatrix.getHeight();
    int width = oldUpdateMatrix.getWidth();
    Computation<SInt> one = builder.numeric().known(BigInteger.ONE);

    return builder.seq((seq2) -> {
      return seq2.par(par1 -> {
        NumericBuilder numeric = par1.numeric();
        Matrix4<Computation<SInt>> lampdas = new Matrix4<>(
            width, height,
            (j) -> {
              ArrayList<Computation<SInt>> newRow = new ArrayList<>(width);
              List<Computation<SInt>> oldRow = oldUpdateMatrix.getRow(j);
              for (int i = 0; i < width; i++) {
                if (j < width - 1) {
                  newRow.add(
                      numeric.mult(L.get(j), oldRow.get(i))
                  );
                } else {
                  newRow.add(numeric.known(BigInteger.ZERO));
                }
              }
              return newRow;
            }
        );
        Computation<Pair<List<Computation<SInt>>, Computation<SInt>>> scaledCAndPP = par1
            .createSequentialSub(seq_pp -> {
              Computation<SInt> pp_inv = seq_pp.createAdvancedNumericBuilder().invert(p_prime);
              Computation<SInt> pp = seq_pp.numeric().mult(p, pp_inv);
              return seq_pp.par((par) -> {
                List<Computation<SInt>> scaledC = new ArrayList<>(C.size());
                for (int j = 0; j < C.size() - 1; j++) {
                  int finalJ = j;
                  scaledC.add(par.createSequentialSub((scaleSeq) -> {
                        Computation<SInt> scaling;
                        scaling = scaleSeq.createSequentialSub(
                            new ConditionalSelect(L.get(finalJ), one, pp_inv)
                        );
                    return scaleSeq.numeric().mult(C.get(finalJ), scaling);
                      })
                  );
                }
                scaledC.add(
                    par.numeric().mult(C.get(C.size() - 1), pp_inv)
                );
                return Pair.lazy(scaledC, pp);
              });
            });
        return () -> new Pair<>(lampdas, scaledCAndPP.out());
      }).par((input, gpAddAndSub) -> {
        Matrix4<Computation<SInt>> lambdas_i_jOuts = input.getFirst();
        List<Computation<SInt>> scaledC = input.getSecond().getFirst();
        Computation<SInt> pp = input.getSecond().getSecond();
        NumericBuilder numeric = gpAddAndSub.numeric();

        List<Computation<SInt>> lambdas_i = new ArrayList<>(height);
        Matrix4<Computation<SInt>> subOuts = new Matrix4<>(width, height,
            (j) -> {
              ArrayList<Computation<SInt>> newRow = new ArrayList<>(width);
              List<Computation<SInt>> oldRow = oldUpdateMatrix.getRow(j);
              List<Computation<SInt>> lambdaRow = lambdas_i_jOuts.getRow(j);
              for (int i = 0; i < width; i++) {
                newRow.add(numeric.sub(
                    oldRow.get(i),
                    lambdaRow.get(i)
                ));
              }
              return newRow;
            });
        for (int i = 0; i < width; i++) {
          lambdas_i.add(
              gpAddAndSub
                  .createSequentialSub(new SumSIntList(
                      lambdas_i_jOuts.getColumn(i)
                  ))
          );
        }
        return () -> new Pair<>(
            new Pair<>(scaledC, pp), new Pair<>(subOuts, lambdas_i));
      });
    }).par(
        (input, gpMults) -> {
          List<Computation<SInt>> scaledC = input.getFirst().getFirst();
          Computation<SInt> pp = input.getFirst().getSecond();
          Matrix4<Computation<SInt>> subOuts = input.getSecond().getFirst();
          List<Computation<SInt>> lambdas_i = input.getSecond().getSecond();
          NumericBuilder numeric = gpMults.numeric();
          Matrix4<Computation<SInt>> mults_cAndLambda_iOuts =
              new Matrix4<>(width, height,
                  (j) -> {
                    ArrayList<Computation<SInt>> mults_cAndLambda_iOuts_row = new ArrayList<>(
                        width);
                    for (int i = 0; i < width; i++) {
                      mults_cAndLambda_iOuts_row
                          .add(numeric.mult(scaledC.get(j), lambdas_i.get(i)));
                    }
                    return mults_cAndLambda_iOuts_row;
                  });
          Matrix4<Computation<SInt>> mults_sub_and_ppOuts = new Matrix4<>(width,
              height,
              (j) -> {
                ArrayList<Computation<SInt>> mults_sub_and_ppOuts_row = new ArrayList<>(width);
                ArrayList<Computation<SInt>> subRow = subOuts.getRow(j);
                for (int i = 0; i < width; i++) {
                  mults_sub_and_ppOuts_row.add(numeric.mult(subRow.get(i), pp));
                }
                return mults_sub_and_ppOuts_row;
              });

          return Pair.lazy(mults_cAndLambda_iOuts, mults_sub_and_ppOuts);
        }
    ).par((pair, adds) -> {
      Matrix4<Computation<SInt>> mults_cAndLambda_iOuts = pair.getFirst();
      Matrix4<Computation<SInt>> mults_sub_and_ppOuts = pair.getSecond();
      NumericBuilder numeric = adds.numeric();
      Matrix4<Computation<SInt>> resultMatrix = new Matrix4<>(width, height,
          (j) -> {
            ArrayList<Computation<SInt>> row = new ArrayList<>(width);
            ArrayList<Computation<SInt>> mults_cAndLambdaRow = mults_cAndLambda_iOuts.getRow(j);
            ArrayList<Computation<SInt>> mult_sub_and_pp_row = mults_sub_and_ppOuts.getRow(j);
            for (int i = 0; i < width; i++) {
              row.add(
                  numeric.add(
                      mults_cAndLambdaRow.get(i),
                      mult_sub_and_pp_row.get(i))
              );
            }
            return row;
          });
      return () -> resultMatrix;
    });
  }

}
