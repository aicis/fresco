/*******************************************************************************
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

import java.math.BigInteger;
import java.util.Iterator;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.lp.LPPrefix;
import dk.alexandra.fresco.lib.lp.LPTableau;
import dk.alexandra.fresco.lib.lp.Matrix;
import dk.alexandra.fresco.lib.lp.SimpleLPPrefix;

/**
 * A builder to set up the {@link LPPrefix} for a DEA input efficiency analysis. 
 * 
 * <p>
 * The initial LP tableau is set up using the Big-M method to handle the
 * equality and greater-than constraints. To handle the fact that this is 
 * minimization problem and our Simplex solver only maximizes we simply negate 
 * the objective function. This, however, means that we must negate the
 * result of the Simplex solver in order to get the correct result.
 * </p>
 *
 */
public class DEAInputEfficiencyPrefixBuilder extends DEAPrefixBuilder {

  /**
   * Builds the LPPrefix for the specified input efficiency DEA problem.
   * <p>
   * Attempts to check if the values given are consistent before building the 
   * prefix. If this is not the case a {@link IllegalStateException}  will be 
   * thrown.
   * </p>
   * @return an LPPrefix
   */
  public LPPrefix build() {
    if (!ready()) {
      throw new IllegalStateException(
          "Builder not ready to build LPPrefix not enough data supplied!");
    }
    if (!consistent()) {
      throw new IllegalStateException("Trying to build LPPrefix from inconsistent data!");
    }
    NumericProtocolBuilder builder = new NumericProtocolBuilder(provider);

    if (this.prefix != null) {
      builder.addProtocolProducer(this.prefix);
    }
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
    SInt one = builder.known(1);
    SInt negOne = builder.known(-1);
    SInt zero = builder.known(0);
    OInt oBigM = provider.getOInt(BigInteger.valueOf(-bigM));
    SInt sBigM = builder.known(-bigM);
    SInt z = builder.known(-bigM);
    SInt[][] c = new SInt[constraints][variables];
    SInt[] b = new SInt[constraints];
    SInt[] f = new SInt[variables];
    SInt pivot = one;
    
    SInt[][] u = new SInt[constraints + 1][constraints + 1];
    Iterator<SInt[]> basisIt = basisInputs.iterator();
    Iterator<SInt> targetIt = targetInputs.iterator();
    int i = 0;
    builder.beginParScope();
    // Set up constraints related to the inputs
    for (; i < inputs; i++) {
      SInt tValue = targetIt.next();
      SInt[] bValues = basisIt.next();
      c[i][0] = builder.sub(zero, tValue);
      b[i] = zero;
      int j = 1;
      for (; j < dbSize + 1; j++) {
        c[i][j] = bValues[j - 1];
      }
      for (; j < variables; j++) {
        c[i][j] = (j - (dbSize + 1) == i) ? one : zero;
      }
    }
    // Set up constraints related to the outputs
    basisIt = basisOutputs.iterator();
    targetIt = targetOutputs.iterator();
    for (; i < inputs + outputs; i++) {
      SInt tValue = targetIt.next();
      SInt[] bValues = basisIt.next();
      c[i][0] = zero;
      b[i] = tValue;
      int j = 1;
      for (; j < dbSize + 1; j++) {
        c[i][j] = bValues[j - 1];
      }
      for (; j < dbSize + 1 + constraints; j++) {
        c[i][j] = (j - (dbSize + 1) == i) ? one : zero;        
      }
      for (; j < variables; j++) {
        c[i][j] = (j - (dbSize + 1 + constraints) == i - inputs) ? negOne : zero;        
      }
    }
    // Set up constraints related to the lambda values
    c[i][0] = zero;
    b[i] = one;
    int j = 1;
    for (; j < dbSize + 1; j++) {
      c[i][j] = one;
    }
    for (; j < variables; j++) {
      c[i][j] = (j - (dbSize+1) == i) ? one : zero;
    }
    // Set up the f vector, i.e., tableau row related to the objective function
    // The theta variable
    f[0] = one; 
    // -bigM for the lambda variables from the equality constraint 
    for (int k = 1; k < variables; k++) {
      f[k] = (k < dbSize + 1) ? sBigM : zero;
    }
    // +bigM for the artificial variables from the greater than constraint
    for (int k = (1 + dbSize + constraints); k < variables; k++) {
      f[k] = builder.sub(zero, sBigM);
    }
    // Add to the lambda variables -bigM*value for each of the output values
    // In other words subtract bigM times each of the tableau rows associated 
    // with an output constraint.
    builder.beginSeqScope();
    for (int l = inputs; l < inputs+outputs; l++) {
      builder.beginParScope();
      for (int k = 1; k < dbSize + 1; k++) {
        builder.beginSeqScope();
        SInt scaled = builder.mult(sBigM, c[l][k]);
        f[k] = builder.add(scaled, f[k]);
        builder.endCurScope();
      }     
      builder.beginSeqScope();
      SInt scaled = builder.mult(oBigM, b[l]);
      z = builder.add(scaled, z);
      builder.endCurScope();
      builder.endCurScope();
    }
    builder.endCurScope();
    builder.endCurScope();
    builder.beginParScope();
    for (int k = 0; k < u.length; k++) {
      for (int l = 0; l < u[k].length; l++) {
        u[k][l] = (k == l) ? builder.known(1) : builder.known(0);
      }
    }
    // Finally make copies of all values to avoid issues with mutable SInts
    for (int k = 0; k < c.length; k++) {
      for (int l = 0; l < c[k].length; l++) {
        SInt copy = builder.getSInt();
        builder.copy(copy, c[k][l]);
        c[k][l] = copy;
      }
    }
    for (int k = 0; k < f.length; k++) {
      SInt copy = builder.getSInt();
      builder.copy(copy, f[k]);
      f[k] = copy;
    }
    for (int k = 0; k < b.length; k++) {
      SInt copy = builder.getSInt();
      builder.copy(copy, b[k]);
      b[k] = copy;
    }
    SInt copy = builder.getSInt();
    builder.copy(copy, pivot);
    pivot = copy;
    builder.endCurScope();
    return new SimpleLPPrefix(new Matrix<SInt>(u), new LPTableau(new Matrix<SInt>(c), b, f, z),
        pivot, new SInt[constraints], builder.getProtocol());
  }
  
  @Override
  public DEAPrefixBuilder createNewInstance() {
    return new DEAInputEfficiencyPrefixBuilder();
  }

}
