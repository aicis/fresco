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
public class DEAPrefixBuilderMaximize implements
    Computation<SimpleLPPrefix, ProtocolBuilderNumeric> {

  private final List<List<DRes<SInt>>> basisInputs;
  private final List<List<DRes<SInt>>> basisOutputs;
  private final List<DRes<SInt>> targetInputs;
  private final List<DRes<SInt>> targetOutputs;

  public DEAPrefixBuilderMaximize(
      List<List<DRes<SInt>>> basisInputs, List<List<DRes<SInt>>> basisOutputs,
      List<DRes<SInt>> targetInputs, List<DRes<SInt>> targetOutputs) {
    this.basisInputs = basisInputs;
    this.basisOutputs = basisOutputs;
    this.targetInputs = targetInputs;
    this.targetOutputs = targetOutputs;
  }

  // A value no benchmarking result should be larger than. Note the benchmarking results are of the form
  // \theta = "the factor a farmer can do better than he currently does" and thus is not necessarily upper bounded.
  private static final int BENCHMARKING_BIG_M = 1000;


  @Override
  public DRes<SimpleLPPrefix> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<SInt> zero = builder.numeric().known(BigInteger.ZERO);
    DRes<SInt> one = builder.numeric().known(BigInteger.ONE);
    /*
     * First copy the target values to the basis. This ensures that the
		 * target values are in the basis thus the score must at least be 1.
		 */
    List<List<DRes<SInt>>> newBasisInputs = addTargetToList(basisInputs, targetInputs);
    List<List<DRes<SInt>>> newBasisOutputs = addTargetToList(basisOutputs, targetOutputs);

		/*
     * NeProtocol the basis output
		 */
    int lambdas = newBasisInputs.get(0).size();

    BigInteger negativeOne = BigInteger.valueOf(-1);

    DRes<List<List<DRes<SInt>>>> negatedBasisOutputsComputation =
        builder.par((par) -> {
          Numeric numeric = par.numeric();
          List<List<DRes<SInt>>> negatedBasisResult = newBasisOutputs.stream().map(
              outputs -> outputs.stream()
                  .map(output -> numeric.mult(negativeOne, output))
                  .collect(Collectors.toList())
          ).collect(Collectors.toList());
          return () -> negatedBasisResult;
        });

    int constraints = newBasisInputs.size() + newBasisOutputs.size() + 1;
    int variables = lambdas + constraints + 1;
    ArrayList<ArrayList<DRes<SInt>>> slack = getIdentity(constraints, one, zero);
    ArrayList<ArrayList<DRes<SInt>>> C = new ArrayList<>(constraints);
    for (int i = 0; i < newBasisInputs.size(); i++) {
      C.add(inputRow(newBasisInputs.get(i), slack.get(i), zero));
    }

    return builder.seq(seq -> {

      List<List<DRes<SInt>>> negatedBasisOutputs = negatedBasisOutputsComputation.out();

      for (int i = newBasisInputs.size(); i < constraints - 1; i++) {
        C.add(outputRow(negatedBasisOutputs.get(i - newBasisInputs.size()),
            targetOutputs.get(i - newBasisInputs.size()), slack.get(i)));
      }
      C.add(lambdaRow(lambdas, slack.get(constraints - 1), zero, one));

      ArrayList<DRes<SInt>> F = fVector(variables, lambdas, seq, zero);
      ArrayList<DRes<SInt>> B = bVector(constraints, targetInputs, zero, one);
      DRes<SInt> z = seq.numeric().known(BigInteger.valueOf(-BENCHMARKING_BIG_M));
      DRes<SInt> pivot = one;

      ArrayList<DRes<SInt>> basis = new ArrayList<>(constraints);
      for (int i = 0; i < constraints; i++) {
        basis.add(seq.numeric().known(BigInteger.valueOf(lambdas + 1 + 1 + i)));
      }

      LPTableau tab = new LPTableau(new Matrix<>(constraints, variables, C), B, F, z);
      Matrix<DRes<SInt>> updateMatrix = new Matrix<>(
          constraints + 1, constraints + 1, getIdentity(constraints + 1, one, zero));
      return () -> new SimpleLPPrefix(updateMatrix, tab, pivot, basis);
    });
  }

  static ArrayList<ArrayList<DRes<SInt>>> getIdentity(int dimension, DRes<SInt> one,
      DRes<SInt> zero) {
    ArrayList<ArrayList<DRes<SInt>>> identity = new ArrayList<>(dimension);
    for (int i = 0; i < dimension; i++) {
      ArrayList<DRes<SInt>> row = new ArrayList<>();
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

  private List<List<DRes<SInt>>> addTargetToList(
      List<List<DRes<SInt>>> basisOutputs,
      List<DRes<SInt>> targetOutputs) {
    ListIterator<List<DRes<SInt>>> basisIt = basisOutputs.listIterator();
    ListIterator<DRes<SInt>> targetIt = targetOutputs.listIterator();
    List<List<DRes<SInt>>> newBasis = new LinkedList<>();
    while (basisIt.hasNext()) {
      List<DRes<SInt>> basisOutput = basisIt.next();
      DRes<SInt> targetOutput = targetIt.next();
      List<DRes<SInt>> newInputs = new ArrayList<>(basisOutput.size() + 1);
      newInputs.addAll(basisOutput);
      newInputs.add(targetOutput);
      newBasis.add(newInputs);
    }
    return newBasis;
  }

  private ArrayList<DRes<SInt>> fVector(int size, int lambdas,
      ProtocolBuilderNumeric builder,
      DRes<SInt> zero) {
    Numeric numeric = builder.numeric();
    DRes<SInt> minusOne = numeric.known(BigInteger.valueOf(-1));
    DRes<SInt> minusBigM = numeric.known(BigInteger.valueOf(0 - BENCHMARKING_BIG_M));
    ArrayList<DRes<SInt>> F = new ArrayList<>(size);
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


  private ArrayList<DRes<SInt>> bVector(int size,
      List<DRes<SInt>> targetInputs,
      DRes<SInt> zero,
      DRes<SInt> one) {
    ArrayList<DRes<SInt>> B = new ArrayList<>(size);
    B.addAll(targetInputs);
    // For each bank output constraint B is zero
    while (B.size() < size - 1) {
      B.add(zero);
    }
    // For the lambda constraint B is one
    B.add(one);
    return B;
  }

  private ArrayList<DRes<SInt>> inputRow(List<DRes<SInt>> vflInputs,
      ArrayList<DRes<SInt>> slackVariables,
      DRes<SInt> zero) {
    ArrayList<DRes<SInt>> row = new ArrayList<>(
        vflInputs.size() + slackVariables.size() + 1);
    row.add(zero);
    row.addAll(vflInputs);
    row.addAll(slackVariables);
    return row;
  }

  private ArrayList<DRes<SInt>> outputRow(List<DRes<SInt>> vflOutputs,
      DRes<SInt> bankOutput,
      ArrayList<DRes<SInt>> slackVariables) {
    ArrayList<DRes<SInt>> row = new ArrayList<>(
        vflOutputs.size() + slackVariables.size() + 1);
    row.add(bankOutput);
    row.addAll(vflOutputs);
    row.addAll(slackVariables);
    return row;
  }

  private ArrayList<DRes<SInt>> lambdaRow(int lambdas,
      ArrayList<DRes<SInt>> slackVariables,
      DRes<SInt> zero,
      DRes<SInt> one) {
    ArrayList<DRes<SInt>> row = new ArrayList<>(lambdas + slackVariables.size() + 1);

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
