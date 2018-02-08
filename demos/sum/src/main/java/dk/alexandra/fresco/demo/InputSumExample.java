package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.math.BigInteger;

public class InputSumExample {

  public InputSumExample(){
  }
  
  /**
   * Run the InputSumExample application.
   * @param sce The SCE to use
   * @param resourcePool The ResourcePool to use  
   * @param network The network to use
   */
  public <ResourcePoolT extends ResourcePool> void runApplication(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce,
      ResourcePoolT resourcePool, Network network) {
    InputApplication inputApp;

    int myId = resourcePool.getMyId();
    int[] inputs = new int[]{1, 2, 3, 7, 8, 12, 15, 17};
    if (myId == 1) {
      // I input
      inputApp = new InputApplication(inputs);
    } else {
      // I do not input
      inputApp = new InputApplication(inputs.length);
    }
    SumAndOutputApplication app = new SumAndOutputApplication(inputApp);

    BigInteger result = sce.runApplication(app, resourcePool, network);
    int sum = 0;
    for (int i : inputs) {
      sum += i;
    }
    System.out.println("Expected result: " + sum + ", Result was: " + result);
  }

  /**
   * Main method for InputSumExample.
   * @param args arguments for the demo
   * @throws IOException if the network fails
   */
  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> util = new CmdLineUtil<>();

    util.parse(args);

    ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric> psConf = util.getProtocolSuite();

    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(psConf, util.getEvaluator());

    ResourcePoolT resourcePool = util.getResourcePool();
    util.startNetwork();
    new InputSumExample().runApplication(sce, resourcePool, util.getNetwork());
    
    util.closeNetwork();
    sce.shutdownSCE();
  }

}
