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
package dk.alexandra.fresco.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.sce.SCE;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.DummyConfiguration;
import dk.alexandra.fresco.suite.dummy.DummyProtocolSuite;


public class TestAESDemo {
	
	@Test
	public void testAESDemo() throws Exception {
		int noPlayers = 2;
		Level logLevel = Level.FINE;
		Reporter.init(logLevel);
		// Since SCAPI currently does not work with ports > 9999 we use fixed ports
		// here instead of relying on ephemeral ports which are often > 9999.
		List<Integer> ports = new ArrayList<Integer>(noPlayers);
		for (int i=1; i<=noPlayers; i++) {
			ports.add(9000 + i*10);
		}
		Map<Integer, NetworkConfiguration> netConf = TestConfiguration.getNetworkConfigurations(noPlayers, ports, logLevel);
		Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
		for (int playerId : netConf.keySet()) {
			TestThreadConfiguration ttc = new TestThreadConfiguration();
			ttc.netConf = netConf.get(playerId);
			ttc.protocolSuiteConf = new DummyConfiguration();
			int noOfVMThreads = 3;
			int noOfThreads = 3;
			ProtocolSuite suite = new DummyProtocolSuite();
			ProtocolEvaluator evaluator = new SequentialEvaluator();
			Storage storage = new InMemoryStorage();
			boolean useSecureConnection = true;
			ttc.sceConf = new TestSCEConfiguration(suite, evaluator, noOfThreads, noOfVMThreads,
					ttc.netConf, storage, useSecureConnection);
			conf.put(playerId, ttc);
		}
		
		TestThreadFactory f = new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new TestThread() {

					@Override
					public void test() throws Exception {
						
						boolean[] input = null;
						if (conf.netConf.getMyId() == 2) {
							input = ByteArithmetic.toBoolean("00112233445566778899aabbccddeeff"); // 128-bit AES plaintext block
						} else if (conf.netConf.getMyId() == 1) {
							input = ByteArithmetic.toBoolean("000102030405060708090a0b0c0d0e0f"); // 128-bit key
						}
						
						AESDemo app = new AESDemo(conf.netConf.getMyId(), input);

						SCE sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
						sce.runApplication(app);

						// Verify output state.
						String expected = "69c4e0d86a7b0430d8cdb78070b4c55a"; // expected cipher
						boolean[] actualBoolean = new boolean[app.result.length];
						for (int i=0; i<app.result.length; i++) {
							actualBoolean[i] = app.result[i].getValue();
						}
						String actual = ByteArithmetic.toHex(actualBoolean);
						Assert.assertEquals(expected, actual);

					}
				};
			}
		};
		
		TestThreadRunner.run(f, conf);
	}
	
	
}
