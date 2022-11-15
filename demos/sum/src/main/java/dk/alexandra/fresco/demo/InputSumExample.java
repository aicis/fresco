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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    int[] inputs1 = new int[]{2, 2, 2, 2, 2, 2, 2, 2};
    int[] inputs2 = new int[]{1, 1, 1, 1, 1, 1, 1, 1};

    List<Integer> myInputs = new ArrayList<>();

    int myArraySize = 7;

    network.sendToAll(ByteBuffer.allocate(4).putInt(myArraySize).array());
    List<byte[]> received = network.receiveFromAll();
    int[] allInputSizes = received.stream().mapToInt(binary -> binary[3]
                    | binary[2] << 8
                    | binary[1] << 16
                    | binary[0] << 24
            ).toArray();

    if (myId == 1) {
      // party input
      myInputs.addAll(Arrays.stream(inputs1).boxed().collect(Collectors.toList()));
      myInputs.addAll(Collections.nCopies(inputs2.length, null));
    } else {
      myInputs.addAll(Collections.nCopies(inputs1.length, null));
      myInputs.addAll(Arrays.stream(inputs2).boxed().collect(Collectors.toList()));
    }
    inputApp = new InputApplication(myInputs);

    // and then to the calculation
    SumAndOutputApplication app = new SumAndOutputApplication(inputApp);
    BigInteger result = sce.runApplication(app, resourcePool, network);
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
    new InputSumExample().runApplication(sce, resourcePool, util.getNetwork());
    
    util.closeNetwork();
    sce.close();
  }

}
