package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;

import java.util.List;

public class SimpleLPPrefix {

  private final Matrix<DRes<SInt>> updateMatrix;
  private final LPTableau tableau;
  private final DRes<SInt> pivot;
  private final List<DRes<SInt>> basis;

  public SimpleLPPrefix(Matrix<DRes<SInt>> updateMatrix, LPTableau tableau,
      DRes<SInt> pivot,
      List<DRes<SInt>> basis) {
    this.updateMatrix = updateMatrix;
    this.tableau = tableau;
    this.pivot = pivot;
    this.basis = basis;
  }


  public LPTableau getTableau() {
    return tableau;
  }

  public Matrix<DRes<SInt>> getUpdateMatrix() {
    return updateMatrix;
  }

  public DRes<SInt> getPivot() {
    return pivot;
  }

  public List<DRes<SInt>> getBasis() {
    return basis;
  }
}
