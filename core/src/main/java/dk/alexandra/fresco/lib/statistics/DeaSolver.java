package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.lp.LPSolver;
import dk.alexandra.fresco.lib.lp.LPSolver.LPOutput;
import dk.alexandra.fresco.lib.lp.LPSolver.PivotRule;
import dk.alexandra.fresco.lib.lp.LPTableau;
import dk.alexandra.fresco.lib.lp.OptimalValue;
import dk.alexandra.fresco.lib.lp.SimpleLPPrefix;
import dk.alexandra.fresco.lib.statistics.DeaSolver.DeaResult;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * NativeProtocol for solving DEA problems.
 *
 * <p>
 * Given a dataset (two matrices of inputs and outputs) and a number of query vectors, the protocol
 * will compute how well the query vectors perform compared to the dataset.
 *
 * The result/score of the computation must be converted to a double using Gauss reduction to be
 * meaningful. See the DEASolverTests for an example.
 * </p>
 */
public class DeaSolver implements Application<List<DeaResult>, ProtocolBuilderNumeric> {

  private final List<List<DRes<SInt>>> targetInputs;
  private final List<List<DRes<SInt>>> targetOutputs;
  private final List<List<DRes<SInt>>> inputDataSet;
  private final List<List<DRes<SInt>>> outputDataSet;

  private final AnalysisType type;
  private final PivotRule pivotRule;

  /**
   * Construct a DEA problem for the solver to solve. The problem consists of 4 matrixes: 2 basis
   * input/output matrices containing the dataset which the queries will be measured against 2 query
   * input/output matrices containing the data to be evaluated.
   *
   * @param type The type of analysis to do
   * @param inputValues Matrix of query input values
   * @param outputValues Matrix of query output values
   * @param setInput Matrix containing the basis input
   * @param setOutput Matrix containing the basis output
   */
  public DeaSolver(AnalysisType type, List<List<DRes<SInt>>> inputValues,
      List<List<DRes<SInt>>> outputValues, List<List<DRes<SInt>>> setInput,
      List<List<DRes<SInt>>> setOutput) throws MPCException {
    this(PivotRule.DANZIG, type, inputValues, outputValues, setInput, setOutput);
  }

  /**
   * Construct a DEA problem for the solver to solve. The problem consists of 4 matrixes: 2 basis
   * input/output matrices containing the dataset which the queries will be measured against 2 query
   * input/output matrices containing the data to be evaluated.
   *
   * @param pivotRule the pivot rule to use in LP solver
   * @param type The type of analysis to do
   * @param inputValues Matrix of query input values
   * @param outputValues Matrix of query output values
   * @param setInput Matrix containing the basis input
   * @param setOutput Matrix containing the basis output
   */
  public DeaSolver(PivotRule pivotRule, AnalysisType type, List<List<DRes<SInt>>> inputValues,
      List<List<DRes<SInt>>> outputValues, List<List<DRes<SInt>>> setInput,
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
  public DRes<List<DeaResult>> buildComputation(ProtocolBuilderNumeric builder) {
    List<DRes<SimpleLPPrefix>> prefixes = getPrefixWithSecretSharedValues(builder);
    return builder.par((par) -> {
      List<DRes<Pair<Pair<List<DRes<SInt>>, List<DRes<SInt>>>, DRes<SInt>>>> result =
          new ArrayList<>(targetInputs.size());
      for (int i = 0; i < targetInputs.size(); i++) {
        SimpleLPPrefix prefix = prefixes.get(i).out();
        DRes<SInt> pivot = prefix.getPivot();
        LPTableau tableau = prefix.getTableau();
        Matrix<DRes<SInt>> update = prefix.getUpdateMatrix();
        List<DRes<SInt>> initialBasis = prefix.getBasis();

        result.add(par.seq(subSeq -> subSeq.seq((solverSec) -> {
          LPSolver lpSolver = new LPSolver(pivotRule, tableau, update, pivot, initialBasis);
          DRes<LPOutput> lpOutput = lpSolver.buildComputation(solverSec);
          return lpOutput;
        }).seq((optSec, lpOutput) -> {
          // Compute peers from lpOutput
          DRes<SInt> invPivot = optSec.advancedNumeric().invert(lpOutput.pivot);
          List<DRes<SInt>> column = new LinkedList<>(tableau.getB());
          column.add(tableau.getZ());
          List<ArrayList<DRes<SInt>>> umRows = lpOutput.updateMatrix.getRows();
          // The first index representing a possible peer as defined by the prefixes
          BigInteger f = BigInteger.valueOf(2);
          // The last index representing a possible peer as defined by the prefixes
          BigInteger l = BigInteger.valueOf(2 + inputDataSet.size() - 1);
          l = (type == AnalysisType.INPUT_EFFICIENCY) ? l : l.add(BigInteger.ONE);
          DRes<SInt> firstPeer = optSec.numeric().known(f);
          DRes<SInt> lastPeer = optSec.numeric().known(l);
          // These could be done in parallel
          return optSec.par(innerPar -> {
            List<DRes<SInt>> upColumn =
                umRows.stream().map(row -> innerPar.advancedNumeric().innerProduct(row, column))
                    .collect(Collectors.toList());
            return () -> upColumn;
          }).par((innerPar, upColumn) -> {
            upColumn.remove(upColumn.size() - 1);
            List<DRes<SInt>> basisValues = upColumn.stream()
                .map(n -> innerPar.numeric().mult(invPivot, n)).collect(Collectors.toList());
            return () -> basisValues;
          }).par((innerPar, basisValues) -> {
            List<DRes<SInt>> above =
                lpOutput.basis.stream().map(n -> innerPar.comparison().compareLEQ(firstPeer, n))
                    .collect(Collectors.toList());
            List<DRes<SInt>> below =
                lpOutput.basis.stream().map(n -> innerPar.comparison().compareLEQ(n, lastPeer))
                    .collect(Collectors.toList());
            List<List<DRes<SInt>>> newState = new ArrayList<>(3);
            newState.add(above);
            newState.add(below);
            newState.add(basisValues);
            return () -> newState;
          }).par((innerPar, state) -> {
            List<DRes<SInt>> inRange = IntStream.range(0, lpOutput.basis.size())
                .mapToObj(n -> innerPar.numeric().mult(state.get(0).get(n), state.get(1).get(n)))
                .collect(Collectors.toList());
            List<List<DRes<SInt>>> newState = new ArrayList<>(2);
            newState.add(inRange);
            newState.add(state.get(2));
            return () -> newState;
          }).par((innerPar, state) -> {
            List<DRes<SInt>> peers = IntStream.range(0, lpOutput.basis.size())
                .mapToObj(n -> innerPar.numeric().mult(lpOutput.basis.get(n), state.get(0).get(n)))
                .collect(Collectors.toList());
            List<DRes<SInt>> peerValues = IntStream.range(0, lpOutput.basis.size())
                .mapToObj(n -> innerPar.numeric().mult(state.get(0).get(n), state.get(1).get(n)))
                .collect(Collectors.toList());
            List<List<DRes<SInt>>> newState = new ArrayList<>(2);
            newState.add(peers);
            newState.add(peerValues);
            return () -> newState;
          }).par((innerPar, state) -> {
            List<DRes<SInt>> peersFromZero = state.get(0).stream()
                .map(n -> innerPar.numeric().sub(n, f)).collect(Collectors.toList());
            List<List<DRes<SInt>>> newState = new ArrayList<>(2);
            newState.add(peersFromZero);
            newState.add(state.get(1));
            return () -> newState;
          }).seq((seq, state) -> Pair.lazy(new Pair<>(state.get(0), state.get(1)),
              new OptimalValue(lpOutput.updateMatrix, lpOutput.tableau, lpOutput.pivot)
                  .buildComputation(seq)));
        })));
      }
      return () -> result;
    }).seq((seq, result) -> {
      List<DeaResult> convertedResult =
          result.stream().map(DeaResult::new).collect(Collectors.toList());
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
        prefixes.add(
            builder.seq(new DEAInputEfficiencyPrefixBuilder(
                Collections.unmodifiableList(basisInputs),
                Collections.unmodifiableList(basisOutputs),
                targetInputs.get(i), targetOutputs.get(i))));
      } else {
        prefixes.add(
            builder.seq(new DEAPrefixBuilderMaximize(
                Collections.unmodifiableList(basisInputs),
                Collections.unmodifiableList(basisOutputs),
                targetInputs.get(i), targetOutputs.get(i))));
      }
    }
    return prefixes;
  }

  public enum AnalysisType {
    INPUT_EFFICIENCY, OUTPUT_EFFICIENCY
  }

  public static class DeaResult {

    public final List<SInt> peers;
    public final List<SInt> peerValues;
    public final SInt optimal;

    private DeaResult(DRes<Pair<Pair<List<DRes<SInt>>, List<DRes<SInt>>>, DRes<SInt>>> output) {
      Pair<Pair<List<DRes<SInt>>, List<DRes<SInt>>>, DRes<SInt>> out = output.out();
      this.peers = out.getFirst().getFirst().stream().map(DRes::out).collect(Collectors.toList());
      this.peerValues =
          out.getFirst().getSecond().stream().map(DRes::out).collect(Collectors.toList());
      this.optimal = out.getSecond().out();
    }
  }
}
