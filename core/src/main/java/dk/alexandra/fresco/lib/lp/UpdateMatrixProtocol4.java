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
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ConditionalSelect;
import dk.alexandra.fresco.lib.math.integer.SumSIntList;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateMatrixProtocol4 implements ComputationBuilder<Matrix<SInt>> {

  private Matrix<SInt> oldUpdateMatrix;
  private SInt[] L, C;
  private SInt p, p_prime;

  UpdateMatrixProtocol4(Matrix<SInt> oldUpdateMatrix, SInt[] L, SInt[] C, SInt p,
      SInt p_prime) {
    this.oldUpdateMatrix = oldUpdateMatrix;
    this.L = L;
    this.C = C;
    this.p = p;
    this.p_prime = p_prime;
  }


  @Override
  public Computation<Matrix<SInt>> build(SequentialProtocolBuilder builder) {
    int height = oldUpdateMatrix.getHeight();
    int width = oldUpdateMatrix.getWidth();
    Computation<SInt> one = builder.numeric().known(BigInteger.ONE);

    return builder.seq((seq2) -> {
      return seq2.par(par1 -> {
        NumericBuilder numeric = par1.numeric();
        List<List<Computation<SInt>>> lampdas = new ArrayList<>();
        for (int j = 0; j < height; j++) {
          List<Computation<SInt>> lampdasRow = new ArrayList<>(width);
          for (int i = 0; i < width; i++) {
            if (j < width - 1) {
              lampdasRow.add(
                  numeric.mult(L[j], oldUpdateMatrix.getElement(j, i))
              );
            } else {
              lampdasRow.add(numeric.known(BigInteger.ZERO));
            }
          }
          lampdas.add(lampdasRow);
        }

        Computation<Pair<List<Computation<SInt>>, Computation<SInt>>> scaledCAndPP = par1
            .createSequentialSub(seq_pp -> {
              Computation<SInt> pp_inv = seq_pp.createAdvancedNumericBuilder().invert(p_prime);
              Computation<SInt> pp = seq_pp.numeric().mult(p, pp_inv);
              return seq_pp.par((par) -> {
                List<Computation<SInt>> scaledC = new ArrayList<>(C.length);
                for (int j = 0; j < C.length - 1; j++) {
                  int finalJ = j;
                  scaledC.add(par.createSequentialSub((scaleSeq) -> {
                        Computation<SInt> scaling;
                        scaling = scaleSeq.createSequentialSub(
                            new ConditionalSelect(L[finalJ], one, pp_inv)
                        );
                        return scaleSeq.numeric().mult(C[finalJ], scaling);
                      })
                  );
                }
                scaledC.add(
                    par.numeric().mult(C[C.length - 1], pp_inv)
                );
                return Pair.lazy(scaledC, pp);
              });
            });
        return () -> new Pair<>(lampdas, scaledCAndPP.out());
      }).par((input, gpAddAndSub) -> {
        List<List<Computation<SInt>>> lambdas_i_jOuts = input.getFirst();
        List<Computation<SInt>> scaledC = input.getSecond().getFirst();
        Computation<SInt> pp = input.getSecond().getSecond();
        NumericBuilder numeric = gpAddAndSub.numeric();
        List<List<Computation<SInt>>> subOuts = new ArrayList<>(height);
        List<Computation<SInt>> lambdas_i = new ArrayList<>(height);
        for (int j = 0; j < height; j++) {
          List<Computation<SInt>> subOutsRow = new ArrayList<>(width);
          for (int i = 0; i < width; i++) {
            subOutsRow.add(numeric.sub(
                oldUpdateMatrix.getElement(j, i),
                lambdas_i_jOuts.get(j).get(i)
            ));
          }
          subOuts.add(subOutsRow);
        }
        for (int i = 0; i < width; i++) {
          lambdas_i.add(
              gpAddAndSub
                  .createSequentialSub(new SumSIntList(
                      //TODO Use Matrix4
                      Arrays.asList(
                          new Matrix<>(lambdas_i_jOuts.stream().map(
                              row -> row.stream().map(Computation::out).toArray(SInt[]::new)
                          ).toArray(SInt[][]::new)).getIthColumn(i, new SInt[height]))
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
          List<List<Computation<SInt>>> subOuts = input.getSecond().getFirst();
          List<Computation<SInt>> lambdas_i = input.getSecond().getSecond();
          NumericBuilder numeric = gpMults.numeric();
          List<List<Computation<SInt>>> mults_cAndLambda_iOuts = new ArrayList<>(height);
          List<List<Computation<SInt>>> mults_sub_and_ppOuts = new ArrayList<>(height);
          for (int j = 0; j < height; j++) {
            List<Computation<SInt>> mults_cAndLambda_iOuts_row = new ArrayList<>(width);
            List<Computation<SInt>> mults_sub_and_ppOuts_row = new ArrayList<>(width);
            for (int i = 0; i < width; i++) {
              mults_cAndLambda_iOuts_row.add(numeric.mult(scaledC.get(j), lambdas_i.get(i)));
              mults_sub_and_ppOuts_row.add(numeric.mult(subOuts.get(j).get(i), pp));
            }
            mults_cAndLambda_iOuts.add(mults_cAndLambda_iOuts_row);
            mults_sub_and_ppOuts.add(mults_sub_and_ppOuts_row);
          }

          return Pair.lazy(mults_cAndLambda_iOuts, mults_sub_and_ppOuts);
        }
    ).par((pair, adds) -> {
      List<List<Computation<SInt>>> mults_cAndLambda_iOuts = pair.getFirst();
      List<List<Computation<SInt>>> mults_sub_and_ppOuts = pair.getSecond();
      NumericBuilder numeric = adds.numeric();
      List<List<Computation<SInt>>> newMatrix = new ArrayList<>(height);
      for (int j = 0; j < height; j++) {
        List<Computation<SInt>> row = new ArrayList<>(width);
        for (int i = 0; i < width; i++) {
          row.add(
              numeric.add(
                  mults_cAndLambda_iOuts.get(j).get(i),
                  mults_sub_and_ppOuts.get(j).get(i))
          );
        }
        newMatrix.add(row);
      }

      return () ->
          new Matrix<>(newMatrix.stream().map(
              row -> row.stream().map(Computation::out).toArray(SInt[]::new)
          ).toArray(SInt[][]::new));
    });
  }

}
