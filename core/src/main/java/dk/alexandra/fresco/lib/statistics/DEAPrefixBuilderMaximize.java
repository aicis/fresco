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
package dk.alexandra.fresco.lib.statistics;


import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.lp.LPTableau;
import dk.alexandra.fresco.lib.lp.Matrix;
import dk.alexandra.fresco.lib.lp.SimpleLPPrefix;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * A helper class used to build the LPPrefix from the SInts representing the
 * input and outputs of DEA instance and the prefix GateProducer that populates
 * these SInts. Note that we use the words "output" and "input" in terms of the
 * DEA instance. I.e. in the way the economists use these words.
 */
public class DEAPrefixBuilderMaximize {

  // A value no benchmarking result should be larger than. Note the benchmarking results are of the form
  // \theta = "the factor a farmer can do better than he currently does" and thus is not necessarily upper bounded.
  private static final int BENCHMARKING_BIG_M = 1000;

  /**
   * Constructs an empty builder
   */
  DEAPrefixBuilderMaximize() {
    super();
  }

  public static Computation<SimpleLPPrefix> build(
      List<SInt[]> basisInputs, List<SInt[]> basisOutputs,
      List<SInt> targetInputs, List<SInt> targetOutputs,
      SequentialProtocolBuilder builder
  ) {
    Computation<SInt> zero = builder.numeric().known(BigInteger.ZERO);
    Computation<SInt> one = builder.numeric().known(BigInteger.ONE);
    /*
     * First copy the target values to the basis. This ensures that the
		 * target values are in the basis thus the score must at least be 1.
		 */
    List<List<SInt>> newBasisInputs = addTargetToList(basisInputs, targetInputs);
    List<List<SInt>> newBasisOutputs = addTargetToList(basisOutputs, targetOutputs);

		/*
     * NeProtocol the basis output
		 */
    int lambdas = newBasisInputs.get(0).size();

    BigInteger negativeOne = BigInteger.valueOf(-1);

    Computation<List<List<Computation<SInt>>>> negatedBasisOutputsComputation =
        builder.par((par) -> {
          NumericBuilder numeric = par.numeric();
          List<List<Computation<SInt>>> negatedBasisResult = newBasisOutputs.stream().map(
              outputs -> outputs.stream()
                  .map(output -> numeric.mult(negativeOne, output))
                  .collect(Collectors.toList())
          ).collect(Collectors.toList());
          return () -> negatedBasisResult;
        });

    int constraints = newBasisInputs.size() + newBasisOutputs.size() + 1;
    int variables = lambdas + constraints + 1;
    ArrayList<ArrayList<Computation<SInt>>> slack = getIdentity(constraints, one, zero);
    ArrayList<ArrayList<Computation<SInt>>> C = new ArrayList<>(constraints);
    for (int i = 0; i < newBasisInputs.size(); i++) {
      C.add(inputRow(newBasisInputs.get(i), slack.get(i), zero));
    }

    return builder.seq(seq -> {

      List<List<Computation<SInt>>> negatedBasisOutputs = negatedBasisOutputsComputation.out();

      for (int i = newBasisInputs.size(); i < constraints - 1; i++) {
        C.add(outputRow(negatedBasisOutputs.get(i - newBasisInputs.size()),
            targetOutputs.get(i - newBasisInputs.size()), slack.get(i)));
      }
      C.add(lambdaRow(lambdas, slack.get(constraints - 1), zero, one));

      ArrayList<Computation<SInt>> F = fVector(variables, lambdas, seq, zero);
      ArrayList<Computation<SInt>> B = bVector(constraints, targetInputs, zero, one);
      Computation<SInt> z = seq.numeric().known(BigInteger.valueOf(-BENCHMARKING_BIG_M));
      Computation<SInt> pivot = one;

      ArrayList<Computation<SInt>> basis = new ArrayList<>(constraints);
      for (int i = 0; i < constraints; i++) {
        basis.add(seq.numeric().known(BigInteger.valueOf(lambdas + 1 + 1 + i)));
      }

      LPTableau tab = new LPTableau(new Matrix<>(constraints, variables, C), B, F, z);
      Matrix<Computation<SInt>> updateMatrix = new Matrix<>(
          constraints + 1, constraints + 1, getIdentity(constraints + 1, one, zero));
      return () -> new SimpleLPPrefix(updateMatrix, tab, pivot, basis);
    });
  }


  static ArrayList<ArrayList<Computation<SInt>>> getIdentity(int dimension, Computation<SInt> one,
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

  static List<List<SInt>> addTargetToList(List<SInt[]> basisOutputs, List<SInt> targetOutputs) {
    ListIterator<SInt[]> basisIt = basisOutputs.listIterator();
    ListIterator<SInt> targetIt = targetOutputs.listIterator();
    List<List<SInt>> newBasis = new LinkedList<>();
    while (basisIt.hasNext()) {
      SInt[] basisOutput = basisIt.next();
      SInt targetOutput = targetIt.next();
      List<SInt> newInputs = new ArrayList<>(basisOutput.length + 1);
      newInputs.addAll(Arrays.asList(basisOutput));
      newInputs.add(targetOutput);
      newBasis.add(newInputs);
    }
    return newBasis;
  }

  private static ArrayList<Computation<SInt>> fVector(int size, int lambdas,
      SequentialProtocolBuilder builder,
      Computation<SInt> zero) {
    NumericBuilder numeric = builder.numeric();
    Computation<SInt> minusOne = numeric.known(BigInteger.valueOf(-1));
    Computation<SInt> minusBigM = numeric.known(BigInteger.valueOf(0 - BENCHMARKING_BIG_M));
    ArrayList<Computation<SInt>> F = new ArrayList<>(size);
    int index = 0;
    // Delta has coefficient 1
    F.add(minusOne);
    index++;
    // Make sure there are lambdas > 0
    while (index < lambdas + 1) {
      F.add(minusBigM);
      index++;
    }
    // Slack variables do not contribute to cost
    while (index < size) {
      F.add(zero);
      index++;
    }
    return F;
  }


  private static ArrayList<Computation<SInt>> bVector(int size, List<SInt> targetInputs,
      Computation<SInt> zero,
      Computation<SInt> one) {
    ArrayList<Computation<SInt>> B = new ArrayList<>(size);
    B.addAll(targetInputs);
    // For each bank output constraint B is zero
    while (B.size() < size - 1) {
      B.add(zero);
    }
    // For the lambda constraint B is one
    B.add(one);
    return B;
  }

  private static ArrayList<Computation<SInt>> inputRow(List<SInt> vflInputs,
      ArrayList<Computation<SInt>> slackVariables,
      Computation<SInt> zero) {
    ArrayList<Computation<SInt>> row = new ArrayList<>(
        vflInputs.size() + slackVariables.size() + 1);
    row.add(zero);
    row.addAll(vflInputs);
    row.addAll(slackVariables);
    return row;
  }

  private static ArrayList<Computation<SInt>> outputRow(List<Computation<SInt>> vflOutputs,
      SInt bankOutput,
      ArrayList<Computation<SInt>> slackVariables) {
    ArrayList<Computation<SInt>> row = new ArrayList<>(
        vflOutputs.size() + slackVariables.size() + 1);
    row.add(bankOutput);
    row.addAll(vflOutputs);
    row.addAll(slackVariables);
    return row;
  }

  private static ArrayList<Computation<SInt>> lambdaRow(int lambdas,
      ArrayList<Computation<SInt>> slackVariables,
      Computation<SInt> zero,
      Computation<SInt> one) {
    ArrayList<Computation<SInt>> row = new ArrayList<>(lambdas + slackVariables.size() + 1);

    row.add(zero);

    int index = 0;
    index++;
    while (index < lambdas + 1) {
      row.add(one);
      index++;
    }
    row.addAll(slackVariables);
    return row;
  }

}
