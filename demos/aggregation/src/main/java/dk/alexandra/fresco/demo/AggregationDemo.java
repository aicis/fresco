package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.EncryptAndRevealStep.RowWithCipher;
import dk.alexandra.fresco.demo.helpers.ResourcePoolHelper;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregationDemo<ResourcePoolT extends ResourcePool> {

  public AggregationDemo() {
  }

  /**
   * @return Generates mock input data.
   */
  public List<List<BigInteger>> readInputs() {
    return Arrays.asList(Arrays.asList(BigInteger.valueOf(1), BigInteger.valueOf(10)),
        Arrays.asList(BigInteger.valueOf(1), BigInteger.valueOf(7)),
        Arrays.asList(BigInteger.valueOf(2), BigInteger.valueOf(100)),
        Arrays.asList(BigInteger.valueOf(1), BigInteger.valueOf(50)),
        Arrays.asList(BigInteger.valueOf(3), BigInteger.valueOf(15)),
        Arrays.asList(BigInteger.valueOf(3), BigInteger.valueOf(15)),
        Arrays.asList(BigInteger.valueOf(2), BigInteger.valueOf(70)));
  }

  /**
   * @param result Prints result values to console.
   */
  public void writeOutputs(List<List<BigInteger>> result) {
    for (List<BigInteger> row : result) {
      for (BigInteger value : row) {
        System.out.print(value + " ");
      }
      System.out.println();
    }
  }

  /**
   * @return Uses deterministic encryption (in this case MiMC) for encrypt, under MPC, the values in
   * the specified column, and opens the resulting cipher texts. The resulting OInts are appended to
   * the end of each row.
   *
   * NOTE: This leaks the equality of the encrypted input values.
   *
   * Example: ([k], [v]) -> ([k], [v], enc(k)) for columnIndex = 0
   */
  public List<RowWithCipher> encryptAndReveal(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce,
      List<List<SInt>> inputRows, int columnIndex, ResourcePoolT rp) throws IOException {
    EncryptAndRevealStep ear = new EncryptAndRevealStep(inputRows, columnIndex);
    return sce.runApplication(ear, rp);
  }

  /**
   * @return Takes in a secret-shared collection of rows (2d-array) and returns the secret-shared
   * result of a sum aggregation of the values in the agg column grouped by the values in the key
   * column.
   *
   * This method invokes encryptAndReveal and the aggregate step.
   *
   * Example: ([1], [2]), ([1], [3]), ([2], [4]) -> ([1], [5]), ([2], [4]) for keyColumn = 0 and
   * aggColumn = 1
   */
  public List<List<SInt>> aggregate(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce, ResourcePoolT rp,
      List<List<SInt>> inputRows, int keyColumn, int aggColumn) throws IOException {
    // TODO: need to shuffle input rows and result
    List<RowWithCipher> rowsWithOpenenedCiphers =
        encryptAndReveal(sce, inputRows, keyColumn, rp);
    AggregateStep aggStep = new AggregateStep(rowsWithOpenenedCiphers, keyColumn, aggColumn);
    return sce.runApplication(aggStep, rp);
  }

  /**
   * @return Runs the input step which secret shares all int values in inputRows. Returns and SInt
   * array containing the resulting shares.
   */
  public List<List<SInt>> secretShare(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce,
      List<List<BigInteger>> inputRows, int pid, ResourcePoolT rp) throws IOException {
    InputStep inputStep = new InputStep(inputRows, pid);
    return sce.runApplication(inputStep, rp);
  }

  /**
   * @return Runs the output step which opens all secret shares.
   */
  public List<List<BigInteger>> open(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce,
      List<List<SInt>> secretShares, ResourcePoolT rp) throws IOException {
    OutputStep outputStep = new OutputStep(secretShares);
    return sce.runApplication(outputStep, rp);
  }

  public void runApplication(SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce,
      ResourcePoolT rp)
      throws IOException {
    int keyColumnIndex = 0;
    int aggColumnIndex = 1;

    // Read inputs. For now this just returns a hard-coded array of values.
    List<List<BigInteger>> inputRows = readInputs();

    // Secret-share the inputs.
    List<List<SInt>> secretSharedRows = secretShare(sce, inputRows, rp.getMyId(), rp);

    // Aggregate
    List<List<SInt>> aggregated =
        aggregate(sce, rp, secretSharedRows, keyColumnIndex, aggColumnIndex);

    // Recombine the secret shares of the result
    List<List<BigInteger>> openedResult = open(sce, aggregated, rp);

    // Write outputs. For now this just prints the results to the console.
    writeOutputs(openedResult);

    sce.shutdownSCE();
  }

  public static void main(String[] args) throws IOException {

    // My player ID
    int myPID = Integer.parseInt(args[0]);

    SequentialEvaluator<SpdzResourcePool> sequentialEvaluator = new SequentialEvaluator<>();
    sequentialEvaluator.setMaxBatchSize(4096);

    ProtocolSuite<SpdzResourcePool, ProtocolBuilderNumeric> suite =
        new SpdzProtocolSuite(150, PreprocessingStrategy.DUMMY, null);
    // Instantiate environment
    SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(suite, sequentialEvaluator);

    // Create application we are going run
    AggregationDemo<SpdzResourcePool> app = new AggregationDemo<>();

    SpdzResourcePool rp = ResourcePoolHelper.createResourcePool(suite,
        NetworkingStrategy.KRYONET,
        getNetworkConfiguration(myPID));
    app.runApplication(sce, rp);
  }

  private static NetworkConfiguration getNetworkConfiguration(int myPID) {
    Map<Integer, Party> parties = new HashMap<>();
    parties.put(1, new Party(1, "localhost", 8001));
    parties.put(2, new Party(2, "localhost", 8002));

    return new NetworkConfigurationImpl(myPID, parties);
  }
}
