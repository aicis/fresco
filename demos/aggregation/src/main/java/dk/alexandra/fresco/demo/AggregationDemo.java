package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AggregationDemo<ResourcePoolT extends ResourcePool> {

  /**
   * Generates mock input data.
   *
   * @return mock input matrix
   */
  public Matrix<BigInteger> readInputs() {
    BigInteger[][] rows = {{BigInteger.valueOf(1), BigInteger.valueOf(7)},
        {BigInteger.valueOf(1), BigInteger.valueOf(19)},
        {BigInteger.valueOf(1), BigInteger.valueOf(10)},
        {BigInteger.valueOf(1), BigInteger.valueOf(4)},
        {BigInteger.valueOf(2), BigInteger.valueOf(13)},
        {BigInteger.valueOf(2), BigInteger.valueOf(1)},
        {BigInteger.valueOf(2), BigInteger.valueOf(22)},
        {BigInteger.valueOf(2), BigInteger.valueOf(16)}};
    int h = rows.length;
    int w = rows[0].length;
    ArrayList<ArrayList<BigInteger>> mat = new ArrayList<>();
    for (BigInteger[] row : rows) {
      mat.add(new ArrayList<>(Arrays.asList(row)));
    }
    return new Matrix<>(h, w, mat);
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
      ResourcePoolT rp, Network network) {
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
    Matrix<BigInteger> result = sce.runApplication(aggApp, rp, network);
    writeOutputs(result);
    sce.shutdownSCE();
  }

  /**
   * Main.
   *
   * @param args must include player ID
   * @throws NoSuchAlgorithmException If your system does not support
    the hash function needed by SPDZ
   */
  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws
      IOException, NoSuchAlgorithmException {
    
    
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> util = new CmdLineUtil<>();

    util.parse(args);
    
    ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric> suite = util.getProtocolSuite();

    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(suite, util.getEvaluator());

    ResourcePoolT resourcePool = util.getResourcePool();
    
    AggregationDemo<ResourcePoolT> demo = new AggregationDemo<>();
    
    util.startNetwork();
    demo.runApplication(sce, resourcePool, util.getNetwork());
    
    util.closeNetwork();
    sce.shutdownSCE();
  }

}
