/*******************************************************************************
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
package dk.alexandra.fresco.demo.inputsum;

import dk.alexandra.fresco.demo.CmdLineUtil;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;

public class InputSumExample {

  static void runApplication(int myId, SecureComputationEngine sce) {
    InputApplication inputApp = null;

		int[] inputs = new int[]{1, 2, 3, 7, 8, 12, 15, 17};
		if(myId == 1){
			//I input			
			inputApp = new InputApplication(inputs);
		} else{
			//I do not input
			inputApp = new InputApplication(inputs.length);
		}

		SumAndOutputApplication app = new SumAndOutputApplication(inputApp);

		sce.runApplication(app);

		int sum = 0;
		for(int i : inputs){
			sum+=i;
		}
		System.out.println("Expected result: "+ sum +", Result was: "+ app.getOutput().getValue());
	}

	public static void main(String[] args) {
		int myId = Integer.parseInt(args[0]);	
		CmdLineUtil util = new CmdLineUtil();
		SCEConfiguration sceConf = null;

		util.parse(args);
		sceConf = util.getSCEConfiguration();

    SCEConfiguration sceConf1 = sceConf;
    dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration psConf = util
        .getProtocolSuiteConfiguration();
    SecureComputationEngine sce = new SecureComputationEngineImpl(sceConf1, psConf);

		runApplication(myId, sce);
	}

}