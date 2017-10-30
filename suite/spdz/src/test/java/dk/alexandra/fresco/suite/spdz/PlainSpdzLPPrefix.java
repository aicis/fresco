package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.lp.LPTableau;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PlainSpdzLPPrefix {

  private final Matrix<DRes<SInt>> updateMatrix;
  private final LPTableau tableau;
  private final DRes<SInt> pivot;
  private final ArrayList<DRes<SInt>> basis;

  public PlainSpdzLPPrefix(PlainLPInputReader inputReader, ProtocolBuilderNumeric par)
      throws IOException {
    if (!inputReader.isRead()) {
      inputReader.readInput();
    }
    int noVariables = inputReader.getCostValues().length;
    int noConstraints = inputReader.getConstraintValues().length;
    Matrix<DRes<SInt>> C;
    Numeric numeric = par.numeric();
    C = new Matrix<>(noConstraints, noVariables,
        (i) -> {
          ArrayList<DRes<SInt>> row = new ArrayList<>(noVariables);
          for (int j = 0; j < noVariables; j++) {
            row.add(numeric.known(inputReader.getCValues()[i][j]));
          }
          return row;
        });
    ArrayList<DRes<SInt>> f = new ArrayList<>(
        Arrays.stream(inputReader.getFValues()).map(numeric::known)
            .collect(Collectors.toList()));
    ArrayList<DRes<SInt>> b = new ArrayList<>(
        Arrays.stream(inputReader.getBValues()).map(numeric::known)
            .collect(Collectors.toList()));

    this.updateMatrix = new Matrix<>(noConstraints + 1, noConstraints + 1,
        (i) -> {
          ArrayList<DRes<SInt>> row = new ArrayList<>();
          for (int j = 0; j < noConstraints + 1; j++) {
            if (i == j) {
              row.add(numeric.known(BigInteger.ONE));
            } else {
              row.add(numeric.known(BigInteger.ZERO));
            }
          }
          return row;
        });

    DRes<SInt> z = numeric.known(BigInteger.ZERO);

    this.pivot = numeric.known(BigInteger.ONE);
    this.tableau = new LPTableau(C, b, f, z);
    ArrayList<DRes<SInt>> basis = new ArrayList<>(noConstraints);
    for (int i = 0; i < noConstraints; i++) {
      basis.add(numeric.known(BigInteger.valueOf(noVariables - noConstraints + 1 + i)));
    }
    this.basis = basis;
  }

  public ArrayList<DRes<SInt>> getBasis() {
    return basis;
  }

  public LPTableau getTableau() {
    return this.tableau;
  }

  public Matrix<DRes<SInt>> getUpdateMatrix() {
    return this.updateMatrix;
  }

  public DRes<SInt> getPivot() {
    return this.pivot;
  }

}
