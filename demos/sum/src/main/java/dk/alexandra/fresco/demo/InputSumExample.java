/*
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.math.BigInteger;

public class InputSumExample {

  public static <ResourcePoolT extends ResourcePool> void runApplication(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce,
      ResourcePoolT resourcePool) throws IOException {
    InputApplication inputApp;

    int myId = resourcePool.getMyId();
    int[] inputs = new int[] {1, 2, 3, 7, 8, 12, 15, 17};
    if (myId == 1) {
      // I input
      inputApp = new InputApplication(inputs);
    } else {
      // I do not input
      inputApp = new InputApplication(inputs.length);
    }

    SumAndOutputApplication app = new SumAndOutputApplication(inputApp);

    resourcePool.getNetwork().connect(10000);
    BigInteger result = sce.runApplication(app, resourcePool);
    resourcePool.getNetwork().close();
    int sum = 0;
    for (int i : inputs) {
      sum += i;
    }
    System.out.println("Expected result: " + sum + ", Result was: " + result);
  }

  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> util = new CmdLineUtil<>();

    util.parse(args);

    ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric> psConf =
        (ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric>) util.getProtocolSuite();

    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<ResourcePoolT, ProtocolBuilderNumeric>(psConf,
            util.getEvaluator());

    ResourcePoolT resourcePool = util.getResourcePool();
    runApplication(sce, resourcePool);
  }

}
