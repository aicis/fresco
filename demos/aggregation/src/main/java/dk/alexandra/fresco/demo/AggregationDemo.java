package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.helpers.ResourcePoolHelper;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
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
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixTestUtils;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregationDemo<ResourcePoolT extends ResourcePool> {

  /**
   * Generates mock input data.
   * 
   * @return mock input matrix
   */
  public Matrix<BigInteger> readInputs() {
    MatrixTestUtils utils = new MatrixTestUtils();
    BigInteger[][] rawRows = {{BigInteger.valueOf(1), BigInteger.valueOf(7)},
        {BigInteger.valueOf(1), BigInteger.valueOf(19)},
        {BigInteger.valueOf(1), BigInteger.valueOf(10)},
        {BigInteger.valueOf(1), BigInteger.valueOf(4)},
        {BigInteger.valueOf(2), BigInteger.valueOf(13)},
        {BigInteger.valueOf(2), BigInteger.valueOf(1)},
        {BigInteger.valueOf(2), BigInteger.valueOf(22)},
        {BigInteger.valueOf(2), BigInteger.valueOf(16)}};
    return utils.getInputMatrix(rawRows);
  }

  /**
   * @param result Prints result values to console.
   */
  public void writeOutputs(Matrix<BigInteger> result) {
    for (List<BigInteger> row : result.getRows()) {
      for (BigInteger value : row) {
        System.out.print(value + " ");
      }
      System.out.println();
    }
  }

  /**
   * Executes application.
   * 
   * @param sce the execution environment
   * @param rp resource pool
   */
  public void runApplication(SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce,
      ResourcePoolT rp) {
    int groupByIdx = 0;
    int aggIdx = 1;
    // Create application we are going run
    Application<Matrix<BigInteger>, ProtocolBuilderNumeric> aggApp = root -> {
      DRes<Matrix<DRes<SInt>>> closed;
      // player 1 provides input
      if (root.getBasicNumericContext().getMyId() == 1) {
        closed = root.collections().closeMatrix(readInputs(), 1);
      } else {
        // if we aren't player 1 we need to provide the expected size of the input
        closed = root.collections().closeMatrix(8, 2, 1);
      }
      DRes<Matrix<DRes<SInt>>> aggregated =
          root.collections().leakyAggregateSum(closed, groupByIdx, aggIdx);
      DRes<Matrix<DRes<BigInteger>>> opened = root.collections().openMatrix(aggregated);
      return () -> new MatrixUtils().unwrapMatrix(opened);
    };
    // Run application and get result
    Matrix<BigInteger> result = sce.runApplication(aggApp, rp);
    writeOutputs(result);
    sce.shutdownSCE();
  }

  /**
   * Main.
   * 
   * @param args must include player ID
   */
  public static void main(String[] args) {
    // My player ID
    int pid = Integer.parseInt(args[0]);

    // Define circuit evaluation strategy
    SequentialEvaluator<SpdzResourcePool, ProtocolBuilderNumeric> sequentialEvaluator =
        new SequentialEvaluator<>();
    sequentialEvaluator.setMaxBatchSize(4096);

    // Create SPDZ protocol suite
    ProtocolSuite<SpdzResourcePool, ProtocolBuilderNumeric> suite =
        new SpdzProtocolSuite(150, PreprocessingStrategy.DUMMY, null);

    // Instantiate execution environment
    SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(suite, sequentialEvaluator);

    // Create resource pool
    SpdzResourcePool rp;
    try {
      rp = ResourcePoolHelper.createResourcePool(suite, NetworkingStrategy.KRYONET,
          getNetworkConfiguration(pid));
    } catch (IOException e) {
      System.err.println("Failed to create resource pool.");
      return;
    }

    // Instatiate our demo and run
    AggregationDemo<SpdzResourcePool> demo = new AggregationDemo<>();
    demo.runApplication(sce, rp);
  }

  private static NetworkConfiguration getNetworkConfiguration(int pid) {
    Map<Integer, Party> parties = new HashMap<>();
    parties.put(1, new Party(1, "localhost", 8001));
    parties.put(2, new Party(2, "localhost", 8002));
    return new NetworkConfigurationImpl(pid, parties);
  }
}
