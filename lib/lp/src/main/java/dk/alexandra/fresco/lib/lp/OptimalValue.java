package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.util.ArrayList;

/**
 * Extracts the optimal value from a {@link LPTableau} and an update matrix representing a
 * terminated Simplex method. Returns the corresponding values of <code>score=p/q</code>
 */
public class OptimalValue implements
    Computation<OptimalValue.Result, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SInt>> updateMatrix;
  private final DRes<SInt> pivot;
  private final LPTableau tableau;

  /**
   * An general version of the protocol working any (valid) initial tableau.
   *
   * @param updateMatrix the final update matrix
   * @param tableau the initial tableau
   * @param pivot the final pivot
   */
  public OptimalValue(
      Matrix<DRes<SInt>> updateMatrix,
      LPTableau tableau,
      DRes<SInt> pivot) {
    this.updateMatrix = updateMatrix;
    this.tableau = tableau;
    this.pivot = pivot;
  }

  @Override
  public DRes<Result> buildComputation(ProtocolBuilderNumeric builder) {
    ArrayList<DRes<SInt>> row = updateMatrix.getRow(updateMatrix.getHeight() - 1);
    ArrayList<DRes<SInt>> column = new ArrayList<>(row.size());
    column.addAll(tableau.getB());
    column.add(tableau.getZ());
    AdvancedNumeric advanced = builder.advancedNumeric();
    DRes<SInt> numerator = advanced.innerProduct(row, column);
    DRes<SInt> invDenominator = advanced.invert(pivot);
    DRes<SInt> mult = builder.numeric().mult(numerator, invDenominator);
    return () -> new Result(mult.out(), numerator.out(), pivot.out());
  }

  public static class Result {

    public final SInt optimal;
    public final SInt numerator;
    public final SInt denominator;

    public Result(SInt optimal, SInt numerator, SInt denominator) {
      this.optimal = optimal;
      this.numerator = numerator;
      this.denominator = denominator;
    }
  }
}
