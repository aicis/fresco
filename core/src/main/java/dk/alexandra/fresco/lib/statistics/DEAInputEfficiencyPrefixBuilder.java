/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.lp.LPTableau;
import dk.alexandra.fresco.lib.lp.Matrix;
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
public class DEAInputEfficiencyPrefixBuilder {

  public static Computation<SimpleLPPrefix> build(
      List<SInt[]> basisInputs, List<SInt[]> basisOutputs,
      List<SInt> targetInputs, List<SInt> targetOutputs,
      ProtocolBuilderNumeric builder
  ) {
    NumericBuilder numeric = builder.numeric();
    int inputs = targetInputs.size();
    int outputs = targetOutputs.size();
    int dbSize = basisInputs.get(0).length;
    int constraints = inputs + outputs + 1;
    // One "theta" variable, i.e., the variable to optimize 
    // One variable "lambda" variable for each basis entry
    // One slack variable for each constraint
    // One artificial variable for each and greater than constraint (outputs) 
    int variables = 1 + dbSize + constraints + outputs;
    // 2 should be safe as the optimal value is no larger than 1
    int bigM = 2;
    Computation<SInt> one = numeric.known(BigInteger.valueOf(1));
    Computation<SInt> negOne = numeric.known(BigInteger.valueOf(-1));
    Computation<SInt> zero = numeric.known(BigInteger.valueOf(0));
    BigInteger oBigM = BigInteger.valueOf(-bigM);
    Computation<SInt> sBigM = numeric.known(BigInteger.valueOf(-bigM));
    ArrayList<Computation<SInt>> b = new ArrayList<>(constraints);
    ArrayList<Computation<SInt>> f = new ArrayList<>(variables);
    ArrayList<ArrayList<Computation<SInt>>> c = new ArrayList<>(constraints);

    Computation<SInt> z = builder.par(par -> {
      Computation<SInt> zInner;
      // Set up constraints related to the inputs
      int i = 0;
      Iterator<SInt[]> basisIt = basisInputs.iterator();
      Iterator<SInt> targetIt = targetInputs.iterator();
      for (; i < inputs; i++) {
        ArrayList<Computation<SInt>> row = new ArrayList<>(variables);
        c.add(row);
        SInt tValue = targetIt.next();
        SInt[] bValues = basisIt.next();
        row.add(par.numeric().sub(zero, () -> tValue));
        b.add(zero);
        int j = 1;
        for (; j < dbSize + 1; j++) {
          SInt bValue = bValues[j - 1];
          row.add(() -> bValue);
        }
        for (; j < variables; j++) {
          row.add((j - (dbSize + 1) == i) ? one : zero);
        }
      }
      // Set up constraints related to the outputs
      basisIt = basisOutputs.iterator();
      targetIt = targetOutputs.iterator();
      for (; i < inputs + outputs; i++) {
        ArrayList<Computation<SInt>> row = new ArrayList<>(variables);
        c.add(row);
        SInt tValue = targetIt.next();
        SInt[] bValues = basisIt.next();
        row.add(zero);
        b.add(() -> tValue);
        int j = 1;
        for (; j < dbSize + 1; j++) {
          SInt bValue = bValues[j - 1];
          row.add(() -> bValue);
        }
        for (; j < dbSize + 1 + constraints; j++) {
          row.add((j - (dbSize + 1) == i) ? one : zero);
        }
        for (; j < variables; j++) {
          row.add((j - (dbSize + 1 + constraints) == i - inputs) ? negOne : zero);
        }
      }
      // Set up constraints related to the lambda values
      ArrayList<Computation<SInt>> lambdaRow = new ArrayList<>(variables);
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

      zInner = par.createSequentialSub(seq -> {
        Computation<SInt> zResult = sBigM;
        for (int l = inputs; l < inputs + outputs; l++) {
          Computation<SInt> scaled = seq.numeric().mult(oBigM, b.get(l));
          zResult = seq.numeric().add(scaled, zResult);
        }
        return zResult;
      });

      return zInner;
    });
    // Add to the lambda variables -bigM*value for each of the output values
    // In other words subtract bigM times each of the tableau rows associated
    // with an output constraint.
    return builder.createParallelSub(par -> {
      for (int l = inputs; l < inputs + outputs; l++) {
        int finalL = l;
        par.createSequentialSub(seq -> {
          for (int k = 1; k < dbSize + 1; k++) {
            Computation<SInt> scaled = seq.numeric().mult(sBigM, c.get(finalL).get(k));
            f.set(k, seq.numeric().add(scaled, f.get(k)));
          }
          return () -> null;
        });
      }
      ArrayList<Computation<SInt>> basis = new ArrayList<>(constraints);
      for (int i = 0; i < constraints; i++) {
        basis.add(par.numeric().known(BigInteger.valueOf(1 + dbSize + i + 1)));
      }
      LPTableau tab = new LPTableau(new Matrix<>(constraints, variables, c), b, f, z);
      Matrix<Computation<SInt>> updateMatrix = new Matrix<>(
          constraints + 1, constraints + 1, getIdentity(constraints + 1, one, zero));
      return () -> new SimpleLPPrefix(updateMatrix, tab, one, basis);
    });
  }

  private static ArrayList<ArrayList<Computation<SInt>>> getIdentity(int dimension,
      Computation<SInt> one,
      Computation<SInt> zero) {
    ArrayList<ArrayList<Computation<SInt>>> identity = new ArrayList<>(dimension);
    for (int i = 0; i < dimension; i++) {
      ArrayList<Computation<SInt>> row = new ArrayList<>();
      for (int j = 0; j < dimension; j++) {
        if (i == j) {
          row.add(one);
        } else {
          row.add(zero);
        }
      }
      identity.add(row);
    }
    return identity;
  }

}