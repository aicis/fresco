package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.lp.LPSolver;
import dk.alexandra.fresco.lib.lp.LPSolver.PivotRule;
import dk.alexandra.fresco.lib.lp.LPTableau;
import dk.alexandra.fresco.lib.lp.OptimalValue;
import dk.alexandra.fresco.lib.lp.SimpleLPPrefix;
import dk.alexandra.fresco.lib.statistics.DEASolver.DEAResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NativeProtocol for solving DEA problems.
 *
 * Given a dataset (two matrices of inputs and outputs) and a number of query
 * vectors, the protocol will compute how well the query vectors perform
 * compared to the dataset.
 *
 * The result/score of the computation must be converted to a double using Gauss
 * reduction to be meaningful. See the DEASolverTests for an example.
 */
public class DEASolver implements Application<List<DEAResult>, ProtocolBuilderNumeric> {

  private final List<List<DRes<SInt>>> targetInputs, targetOutputs;
  private final List<List<DRes<SInt>>> inputDataSet, outputDataSet;

  private final AnalysisType type;
  private final PivotRule pivotRule;


  /**
   * Construct a DEA problem for the solver to solve. The problem consists of
   * 4 matrixes: 2 basis input/output matrices containing the dataset which
   * the queries will be measured against
   *
   * 2 query input/output matrices containing the data to be evaluated.
   *
   * @param type The type of analysis to do
   * @param inputValues Matrix of query input values
   * @param outputValues Matrix of query output values
   * @param setInput Matrix containing the basis input
   * @param setOutput Matrix containing the basis output
   */
  public DEASolver(AnalysisType type, List<List<DRes<SInt>>> inputValues,
      List<List<DRes<SInt>>> outputValues,
      List<List<DRes<SInt>>> setInput,
      List<List<DRes<SInt>>> setOutput) throws MPCException {
    this(PivotRule.DANZIG, type, inputValues, outputValues, setInput, setOutput);
  }

  /**
   * Construct a DEA problem for the solver to solve. The problem consists of
   * 4 matrixes: 2 basis input/output matrices containing the dataset which
   * the queries will be measured against
   *
   * 2 query input/output matrices containing the data to be evaluated.
   *
   * @param pivotRule the pivot rule to use in LP solver
   * @param type The type of analysis to do
   * @param inputValues Matrix of query input values
   * @param outputValues Matrix of query output values
   * @param setInput Matrix containing the basis input
   * @param setOutput Matrix containing the basis output
   */
  public DEASolver(
      PivotRule pivotRule,
      AnalysisType type,
      List<List<DRes<SInt>>> inputValues,
      List<List<DRes<SInt>>> outputValues,
      List<List<DRes<SInt>>> setInput,
      List<List<DRes<SInt>>> setOutput) throws MPCException {
    this.pivotRule = pivotRule;
    this.type = type;
    this.targetInputs = inputValues;
    this.targetOutputs = outputValues;
    this.inputDataSet = setInput;
    this.outputDataSet = setOutput;
    if (!consistencyCheck()) {
      throw new MPCException("Inconsistent dataset / query data");
    }
  }

  /**
   * Verify that the input is consistent
   *
   * @return If the input is consistent.
   */
  private boolean consistencyCheck() {

    int inputVariables = inputDataSet.get(0).size();
    int outputVariables = outputDataSet.get(0).size();
    if (inputDataSet.size() != outputDataSet.size()) {
      return false;
    }
    if (targetInputs.size() != targetOutputs.size()) {
      return false;
    }
    for (List<DRes<SInt>> x : targetInputs) {
      if (x.size() != inputVariables) {
        return false;
      }
    }
    for (List<DRes<SInt>> x : inputDataSet) {
      if (x.size() != inputVariables) {
        return false;
      }
    }
    for (List<DRes<SInt>> x : targetOutputs) {
      if (x.size() != outputVariables) {
        return false;
      }
    }
    for (List<DRes<SInt>> x : outputDataSet) {
      if (x.size() != outputVariables) {
        return false;
      }
    }
    return true;
  }

  @Override
  public DRes<List<DEAResult>> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SimpleLPPrefix>> prefixes = getPrefixWithSecretSharedValues(
        builder);
    return builder.par((par) -> {

      List<DRes<Pair<List<DRes<SInt>>, DRes<SInt>>>> result =
          new ArrayList<>(targetInputs.size());
      for (int i = 0; i < targetInputs.size(); i++) {

        SimpleLPPrefix prefix = prefixes.get(i).out();
        DRes<SInt> pivot = prefix.getPivot();
        LPTableau tableau = prefix.getTableau();
        Matrix<DRes<SInt>> update = prefix.getUpdateMatrix();
        List<DRes<SInt>> initialBasis = prefix.getBasis();

        result.add(
            par.seq((subSeq) ->
                subSeq.seq((solverSec) -> {
                  LPSolver lpSolver = new LPSolver(
                      pivotRule, tableau, update, pivot, initialBasis);
                  return lpSolver.buildComputation(solverSec);

                }).seq((optSec, lpOutput) ->
                    Pair.lazy(
                        lpOutput.basis,
                        new OptimalValue(lpOutput.updateMatrix, lpOutput.tableau, lpOutput.pivot)
                            .buildComputation(optSec)
                    )
                ))
        );
      }
      return () -> result;
    }).seq((seq, result) -> {
      List<DEAResult> convertedResult = result.stream().map(DEAResult::new)
          .collect(Collectors.toList());
      return () -> convertedResult;
    });
  }

  private List<DRes<SimpleLPPrefix>> getPrefixWithSecretSharedValues(
      ProtocolBuilderNumeric builder) {
    int dataSetSize = this.inputDataSet.size();

    int noOfSolvers = this.targetInputs.size();
    List<DRes<SimpleLPPrefix>> prefixes = new ArrayList<>(noOfSolvers);

    int lpInputs = this.inputDataSet.get(0).size();
    int lpOutputs = this.outputDataSet.get(0).size();
    List<List<DRes<SInt>>> basisInputs = new ArrayList<>(lpInputs);
    for (int i = 0; i < lpInputs; i++) {
      basisInputs.add(new ArrayList<>(dataSetSize));
    }
    List<List<DRes<SInt>>> basisOutputs = new ArrayList<>(lpOutputs);
    for (int i = 0; i < lpOutputs; i++) {
      basisOutputs.add(new ArrayList<>(dataSetSize));
    }

    for (int i = 0; i < dataSetSize; i++) {
      for (int j = 0; j < inputDataSet.get(i).size(); j++) {
        List<DRes<SInt>> current = inputDataSet.get(i);
        basisInputs.get(j).add(current.get(j));
      }
      for (int j = 0; j < outputDataSet.get(i).size(); j++) {
        List<DRes<SInt>> current = outputDataSet.get(i);
        basisOutputs.get(j).add(current.get(j));
      }
    }
    for (int i = 0; i < noOfSolvers; i++) {
      if (type == AnalysisType.INPUT_EFFICIENCY) {
        prefixes.add(DEAInputEfficiencyPrefixBuilder.build(
            Collections.unmodifiableList(basisInputs), Collections.unmodifiableList(basisOutputs),
            targetInputs.get(i), targetOutputs.get(i),
            builder
        ));
      } else {
        prefixes.add(DEAPrefixBuilderMaximize.build(
            Collections.unmodifiableList(basisInputs), Collections.unmodifiableList(basisOutputs),
            targetInputs.get(i), targetOutputs.get(i),
            builder
        ));
      }
    }
    return prefixes;
  }

  public enum AnalysisType {INPUT_EFFICIENCY, OUTPUT_EFFICIENCY}

  public static class DEAResult {

    public final List<DRes<SInt>> basis;
    public final SInt optimal;

    private DEAResult(DRes<Pair<List<DRes<SInt>>, DRes<SInt>>> output) {
      Pair<List<DRes<SInt>>, DRes<SInt>> out = output.out();
      this.basis = out.getFirst().stream().map(DRes::out).collect(Collectors.toList());
      this.optimal = out.getSecond().out();
    }
  }
}
