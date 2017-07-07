/*
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.demo;

import java.io.IOException;

import dk.alexandra.fresco.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;

public class InputSumExample {

  public static void runApplication(int myId, SecureComputationEngine sce,
      SCEConfiguration sceConf,
      ProtocolSuiteConfiguration protocolSuiteConfig) throws IOException {
    InputApplication inputApp;

    int[] inputs = new int[]{1, 2, 3, 7, 8, 12, 15, 17};
    if (myId == 1) {
      //I input
      inputApp = new InputApplication(inputs);
    } else {
      //I do not input
      inputApp = new InputApplication(inputs.length);
    }

    SumAndOutputApplication app = new SumAndOutputApplication(inputApp);

    sce.runApplication(app, SecureComputationEngineImpl.createResourcePool(sceConf,
        protocolSuiteConfig));

    int sum = 0;
    for (int i : inputs) {
      sum += i;
    }
    System.out.println("Expected result: " + sum + ", Result was: " + app.getResult());
  }

  public static void main(String[] args) throws IOException {
    int myId = Integer.parseInt(args[0]);
    CmdLineUtil util = new CmdLineUtil();
    SCEConfiguration sceConf;

    util.parse(args);
    sceConf = util.getSCEConfiguration();

    dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration psConf =
        util.getProtocolSuiteConfiguration();
    SecureComputationEngine sce = new SecureComputationEngineImpl(psConf,
        sceConf.getEvaluator(), sceConf.getLogLevel(), sceConf.getMyId());

    runApplication(myId, sce, sceConf, psConf);
  }

}