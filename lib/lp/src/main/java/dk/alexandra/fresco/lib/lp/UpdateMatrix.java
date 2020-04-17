package dk.alexandra.fresco.lib.lp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.conditional.ConditionalSelect;

public class UpdateMatrix implements
    Computation<Matrix<DRes<SInt>>, ProtocolBuilderNumeric> {

  private Matrix<DRes<SInt>> oldUpdateMatrix;
  private List<DRes<SInt>> L;
  private List<DRes<SInt>> C;
  private DRes<SInt> p, p_prime;

  UpdateMatrix(Matrix<DRes<SInt>> oldUpdateMatrix, List<DRes<SInt>> L,
      List<DRes<SInt>> C, DRes<SInt> p,
      DRes<SInt> p_prime) {
    this.oldUpdateMatrix = oldUpdateMatrix;
    this.L = L;
    this.C = C;
    this.p = p;
    this.p_prime = p_prime;
  }


  @Override
  public DRes<Matrix<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    int height = oldUpdateMatrix.getHeight();
    int width = oldUpdateMatrix.getWidth();
    DRes<SInt> one = builder.numeric().known(BigInteger.ONE);

    return builder.seq((seq2) -> seq2.par(par1 -> {
      Numeric numeric = par1.numeric();
      Matrix<DRes<SInt>> lampdas = new Matrix<>(
          height, width,
          (j) -> {
            ArrayList<DRes<SInt>> newRow = new ArrayList<>(width);
            List<DRes<SInt>> oldRow = oldUpdateMatrix.getRow(j);
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
      DRes<Pair<List<DRes<SInt>>, DRes<SInt>>> scaledCAndPP =
          par1.seq(seq_pp -> {
            DRes<SInt> pp_inv = seq_pp.advancedNumeric().invert(p_prime);
            DRes<SInt> pp = seq_pp.numeric().mult(p, pp_inv);
            return seq_pp.par((par) -> {
              List<DRes<SInt>> scaledC = new ArrayList<>(C.size());
              for (int j = 0; j < C.size() - 1; j++) {
                int finalJ = j;
                scaledC.add(par.seq((scaleSeq) -> {
                  DRes<SInt> scaling;
                  scaling = scaleSeq.seq(new ConditionalSelect(L.get(finalJ), one, pp_inv));
                  return scaleSeq.numeric().mult(C.get(finalJ), scaling);
                }));
              }
              scaledC.add(
                  par.numeric().mult(C.get(C.size() - 1), pp_inv)
              );
              return Pair.lazy(scaledC, pp);
            });
          });
      return () -> new Pair<>(lampdas, scaledCAndPP.out());
    }).par((gpAddAndSub, input) -> {
      Matrix<DRes<SInt>> lambdas_i_jOuts = input.getFirst();
      List<DRes<SInt>> scaledC = input.getSecond().getFirst();
      DRes<SInt> pp = input.getSecond().getSecond();
      Numeric numeric = gpAddAndSub.numeric();

      List<DRes<SInt>> lambdas_i = new ArrayList<>(height);
      Matrix<DRes<SInt>> subOuts = new Matrix<>(height, width,
          (j) -> {
            ArrayList<DRes<SInt>> newRow = new ArrayList<>(width);
            List<DRes<SInt>> oldRow = oldUpdateMatrix.getRow(j);
            List<DRes<SInt>> lambdaRow = lambdas_i_jOuts.getRow(j);
            for (int i = 0; i < width; i++) {
              newRow.add(numeric.sub(
                  oldRow.get(i),
                  lambdaRow.get(i)
              ));
            }
            return newRow;
          });
      for (int i = 0; i < width; i++) {
        lambdas_i.add(gpAddAndSub.advancedNumeric().sum(lambdas_i_jOuts.getColumn(i)));
      }
      return () -> new Pair<>(
          new Pair<>(scaledC, pp), new Pair<>(subOuts, lambdas_i));
    })).par(
        (gpMults, input) -> {
          List<DRes<SInt>> scaledC = input.getFirst().getFirst();
          DRes<SInt> pp = input.getFirst().getSecond();
          Matrix<DRes<SInt>> subOuts = input.getSecond().getFirst();
          List<DRes<SInt>> lambdas_i = input.getSecond().getSecond();
          Numeric numeric = gpMults.numeric();
          Matrix<DRes<SInt>> mults_cAndLambda_iOuts =
              new Matrix<>(height, width,
                  (j) -> {
                    ArrayList<DRes<SInt>> mults_cAndLambda_iOuts_row = new ArrayList<>(
                        width);
                    for (int i = 0; i < width; i++) {
                      mults_cAndLambda_iOuts_row
                          .add(numeric.mult(scaledC.get(j), lambdas_i.get(i)));
                    }
                    return mults_cAndLambda_iOuts_row;
                  });
          Matrix<DRes<SInt>> mults_sub_and_ppOuts = new Matrix<>(height, width,
              (j) -> {
                ArrayList<DRes<SInt>> mults_sub_and_ppOuts_row = new ArrayList<>(width);
                ArrayList<DRes<SInt>> subRow = subOuts.getRow(j);
                for (int i = 0; i < width; i++) {
                  mults_sub_and_ppOuts_row.add(numeric.mult(subRow.get(i), pp));
                }
                return mults_sub_and_ppOuts_row;
              });

          return Pair.lazy(mults_cAndLambda_iOuts, mults_sub_and_ppOuts);
        }
    ).par((adds, pair) -> {
      Matrix<DRes<SInt>> mults_cAndLambda_iOuts = pair.getFirst();
      Matrix<DRes<SInt>> mults_sub_and_ppOuts = pair.getSecond();
      Numeric numeric = adds.numeric();
      Matrix<DRes<SInt>> resultMatrix = new Matrix<>(height, width,
          (j) -> {
            ArrayList<DRes<SInt>> row = new ArrayList<>(width);
            ArrayList<DRes<SInt>> mults_cAndLambdaRow = mults_cAndLambda_iOuts.getRow(j);
            ArrayList<DRes<SInt>> mult_sub_and_pp_row = mults_sub_and_ppOuts.getRow(j);
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
