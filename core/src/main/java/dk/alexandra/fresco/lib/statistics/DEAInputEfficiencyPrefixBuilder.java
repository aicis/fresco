package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.lp.LPTableau;
import dk.alexandra.fresco.lib.lp.SimpleLPPrefix;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A builder to set up the LP-Prefix for a DEA input efficiency analysis.
 *
 * <p>
 * The initial LP tableau is set up using the Big-M method to handle the
 * equality and greater-than constraints. To handle the fact that this is
 * minimization problem and our Simplex solver only maximizes we simply negate
 * the objective function. This, however, means that we must negate the
 * result of the Simplex solver in order to get the correct result.
 * </p>
 */
public class DEAInputEfficiencyPrefixBuilder implements
Computation<SimpleLPPrefix, ProtocolBuilderNumeric> {

  private final List<List<DRes<SInt>>> basisInputs;
  private final List<List<DRes<SInt>>> basisOutputs;
  private final List<DRes<SInt>> targetInputs;
  private final List<DRes<SInt>> targetOutputs;

  public DEAInputEfficiencyPrefixBuilder(
      List<List<DRes<SInt>>> basisInputs, List<List<DRes<SInt>>> basisOutputs,
      List<DRes<SInt>> targetInputs, List<DRes<SInt>> targetOutputs) {
    this.basisInputs = basisInputs;
    this.basisOutputs = basisOutputs;
    this.targetInputs = targetInputs;
    this.targetOutputs = targetOutputs;
  }

  @Override
  public DRes<SimpleLPPrefix> buildComputation(ProtocolBuilderNumeric builder) {
    Numeric numeric = builder.numeric();
    int inputs = targetInputs.size();
    int outputs = targetOutputs.size();
    int dbSize = basisInputs.get(0).size();
    int constraints = inputs + outputs + 1;
    // One "theta" variable, i.e., the variable to optimize
    // One variable "lambda" variable for each basis entry
    // One slack variable for each constraint
    // One artificial variable for each and greater than constraint (outputs)
    int variables = 1 + dbSize + constraints + outputs;
    // 2 should be safe as the optimal value is no larger than 1
    int bigM = 2;
    DRes<SInt> one = numeric.known(BigInteger.valueOf(1));
    DRes<SInt> negOne = numeric.known(BigInteger.valueOf(-1));
    DRes<SInt> zero = numeric.known(BigInteger.valueOf(0));
    BigInteger oBigM = BigInteger.valueOf(-bigM);
    DRes<SInt> sBigM = numeric.known(BigInteger.valueOf(-bigM));
    ArrayList<DRes<SInt>> b = new ArrayList<>(constraints);
    ArrayList<DRes<SInt>> f = new ArrayList<>(variables);
    ArrayList<ArrayList<DRes<SInt>>> c = new ArrayList<>(constraints);

    DRes<SInt> z = builder.par(par -> {
      DRes<SInt> zInner;
      // Set up constraints related to the inputs
      int i = 0;
      Iterator<List<DRes<SInt>>> basisIt = basisInputs.iterator();
      Iterator<DRes<SInt>> targetIt = targetInputs.iterator();
      for (; i < inputs; i++) {
        ArrayList<DRes<SInt>> row = new ArrayList<>(variables);
        c.add(row);
        DRes<SInt> tValue = targetIt.next();
        List<DRes<SInt>> bValues = basisIt.next();
        row.add(par.numeric().sub(zero, tValue));
        b.add(zero);
        int j = 1;
        for (; j < dbSize + 1; j++) {
          DRes<SInt> bValue = bValues.get(j - 1);
          row.add(bValue);
        }
        for (; j < variables; j++) {
          row.add((j - (dbSize + 1) == i) ? one : zero);
        }
      }
      // Set up constraints related to the outputs
      basisIt = basisOutputs.iterator();
      targetIt = targetOutputs.iterator();
      for (; i < inputs + outputs; i++) {
        ArrayList<DRes<SInt>> row = new ArrayList<>(variables);
        c.add(row);
        DRes<SInt> tValue = targetIt.next();
        List<DRes<SInt>> bValues = basisIt.next();
        row.add(zero);
        b.add(tValue);
        int j = 1;
        for (; j < dbSize + 1; j++) {
          DRes<SInt> bValue = bValues.get(j - 1);
          row.add(bValue);
        }
        for (; j < dbSize + 1 + constraints; j++) {
          row.add((j - (dbSize + 1) == i) ? one : zero);
        }
        for (; j < variables; j++) {
          row.add((j - (dbSize + 1 + constraints) == i - inputs) ? negOne : zero);
        }
      }
      // Set up constraints related to the lambda values
      ArrayList<DRes<SInt>> lambdaRow = new ArrayList<>(variables);
      c.add(lambdaRow);
      lambdaRow.add(zero);
      b.add(one);
      int j = 1;
      for (; j < dbSize + 1; j++) {
        lambdaRow.add(one);
      }
      for (; j < variables; j++) {
        lambdaRow.add((j - (dbSize + 1) == i) ? one : zero);
      }
      // Set up the f vector, i.e., tableau row related to the objective function
      // The theta variable
      f.add(one);
      // -bigM for the lambda variables from the equality constraint
      for (int k = 1; k < variables; k++) {
        f.add((k < dbSize + 1) ? sBigM : zero);
      }
      // +bigM for the artificial variables from the greater than constraint
      for (int k = (1 + dbSize + constraints); k < variables; k++) {
        f.set(k, par.numeric().sub(zero, sBigM));
      }

      zInner = par.seq(seq -> {
        DRes<SInt> zResult = sBigM;
        for (int l = inputs; l < inputs + outputs; l++) {
          DRes<SInt> scaled = seq.numeric().mult(oBigM, b.get(l));
          zResult = seq.numeric().add(scaled, zResult);
        }
        return zResult;
      });

      return zInner;
    });
    // Add to the lambda variables -bigM*value for each of the output values
    // In other words subtract bigM times each of the tableau rows associated
    // with an output constraint.
    return builder.par(par -> {
      for (int l = inputs; l < inputs + outputs; l++) {
        int finalL = l;
        par.seq(seq -> {
          for (int k = 1; k < dbSize + 1; k++) {
            DRes<SInt> scaled = seq.numeric().mult(sBigM, c.get(finalL).get(k));
            f.set(k, seq.numeric().add(scaled, f.get(k)));
          }
          return null;
        });
      }
      ArrayList<DRes<SInt>> basis = new ArrayList<>(constraints);
      for (int i = 0; i < constraints; i++) {
        basis.add(par.numeric().known(BigInteger.valueOf(1 + dbSize + i + 1)));
      }
      LPTableau tab = new LPTableau(new Matrix<>(constraints, variables, c), b, f, z);
      Matrix<DRes<SInt>> updateMatrix = new Matrix<>(
          constraints + 1, constraints + 1,
          DEAPrefixBuilderMaximize.getIdentity(constraints + 1, one, zero));
      return () -> new SimpleLPPrefix(updateMatrix, tab, one, basis);
    });
  }
}