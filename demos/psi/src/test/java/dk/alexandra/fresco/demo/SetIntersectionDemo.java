/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import dk.alexandra.fresco.IntegrationTest;
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
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.TestSCEConfiguration;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.dummy.DummyConfiguration;
//import dk.alexandra.fresco.suite.dummy.DummyConfiguration;
import dk.alexandra.fresco.suite.dummy.DummyProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesConfiguration;
//import dk.alexandra.fresco.suite.tinytables.online.TinyTablesConfiguration;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproConfiguration;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;


public class SetIntersectionDemo {
	
	private Level logLevel = Level.FINE;
	private int noPlayers = 2;
	
	@Test
	public void dummyTest() throws Exception{
		// Generic configuration
		List<Integer> ports = new ArrayList<Integer>(noPlayers);
		for (int i=1; i<=noPlayers; i++) {
			ports.add(9000 + i);
		}
		Map<Integer, NetworkConfiguration> netConf = TestConfiguration.getNetworkConfigurations(noPlayers, ports, logLevel);
		Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
		for (int playerId : netConf.keySet()) {
			TestThreadConfiguration ttc = new TestThreadConfiguration();
			ttc.netConf = netConf.get(playerId);
			
			// Protocol specific configuration
			ttc.protocolSuiteConf =  new DummyConfiguration();
			ProtocolSuite suite = new DummyProtocolSuite();			
			
			// The rest is generic configuration as well
			int noOfVMThreads = 3;
			int noOfThreads = 3;
			
			ProtocolEvaluator evaluator = new SequentialEvaluator();
			Storage storage = new InMemoryStorage();
			boolean useSecureConnection = true;
			ttc.sceConf = new TestSCEConfiguration(suite, evaluator, noOfThreads, noOfVMThreads,
					ttc.netConf, storage, useSecureConnection);
			conf.put(playerId, ttc);
		}
		String[] result = this.setIntersectionDemo(conf);
		Assert.assertTrue(verifyResult(result));
	}

	
	/**
	 * TinyTables requires a preprocessing phase as well as the
	 * actual computation phase.
	 * @throws Exception
	 */
	@Category(IntegrationTest.class)
	@Test
	public void tinyTablesTest() throws Exception{
		// Generic configuration
		List<Integer> ports = new ArrayList<Integer>(noPlayers);
		for (int i=1; i<=noPlayers; i++) {
			ports.add(9000 + i);
		}
		Map<Integer, NetworkConfiguration> netConf = TestConfiguration.getNetworkConfigurations(noPlayers, ports, logLevel);
		Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
		for (int playerId : netConf.keySet()) {
			TestThreadConfiguration ttc = new TestThreadConfiguration();
			ttc.netConf = netConf.get(playerId);
			
			// Protocol specific configuration + suite
			ttc.protocolSuiteConf = getTinyTablesPreproConfiguration(9000+ttc.netConf.getMyId(), playerId);
			ProtocolSuite suite = getTinyTablesPreproProtocolSuite(playerId);
			
			// More generic configuration
			int noOfVMThreads = 3;
			int noOfThreads = 3;
			
			ProtocolEvaluator evaluator = new SequentialEvaluator();
			Storage storage = new InMemoryStorage();
			boolean useSecureConnection = true;
			ttc.sceConf = new TestSCEConfiguration(suite, evaluator, noOfThreads, noOfVMThreads,
					ttc.netConf, storage, useSecureConnection);
			conf.put(playerId, ttc);
		}
		
		// We run the preprocessing and save the resulting tinytables to disk
		this.setIntersectionDemo(conf);
		
		// Preprocessing is complete, now we configure a new instance of the
		// computation and run it
		netConf = TestConfiguration.getNetworkConfigurations(noPlayers, ports, logLevel);
		conf = new HashMap<Integer, TestThreadConfiguration>();
		for (int playerId : netConf.keySet()) {
			TestThreadConfiguration ttc = new TestThreadConfiguration();
			ttc.netConf = netConf.get(playerId);
			
			// These 2 lines are protocol specific, the rest is generic configuration 
			ttc.protocolSuiteConf = getTinyTablesConfiguration(ttc.netConf.getMyId());
			ProtocolSuite suite = getTinyTablesProtocolSuite(playerId);
			
			int noOfVMThreads = 3;
			int noOfThreads = 3;
			
			ProtocolEvaluator evaluator = new SequentialEvaluator();
			Storage storage = new InMemoryStorage();
			boolean useSecureConnection = true;
			ttc.sceConf = new TestSCEConfiguration(suite, evaluator, noOfThreads, noOfVMThreads,
					ttc.netConf, storage, useSecureConnection);
			conf.put(playerId, ttc);
		}
		
		// Finally we run the processing phase and verify the result
		String[] result = this.setIntersectionDemo(conf);
		Assert.assertTrue(verifyResult(result));
	}
	
	//ensure that test files are removed after the test ends. 
	@After
	public void cleanup() {
		for(int i = 0; i < 2; i++) {
			File f = getTinyTablesFile(i);
			if(f.exists()) {
				f.delete();
			}
		}
	}
	

	private boolean verifyResult(String[] result){
		// Expected ciphers
		String[] expected = {"c5cf1e6421d3302430b4c1e1258e23dc",
				"2f512cbe2004159f2a9f432aa23074fe",
				"a5bb0723dd40d10189b8e7e1ab383aa1",
				"687114568afa5846470e5a5e553c639d",
				"1f4e1f637a388bcb9984cf3d16c9243e",
				"a5bb0723dd40d10189b8e7e1ab383aa1",
				"52cd1dbeeb5f1dce0742aebf285e1472",
				"687114568afa5846470e5a5e553c639d"};
		
		for(int j = 0; j< expected.length; j++){
			if(!expected[j].equals(result[j])){
				return false;
			}
		}
		return true;
	}
	

	
	public String[] setIntersectionDemo(Map<Integer, TestThreadConfiguration>conf) throws Exception {
		Reporter.init(logLevel);
		String[] result = new String[8];
		TestThreadFactory f = new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new TestThread() {
					@Override
					public void test() throws Exception {
						boolean[] key = null;
						int[] inputList = null;
						if (conf.netConf.getMyId() == 2) {
							key = ByteArithmetic.toBoolean("00112233445566778899aabbccddeeff"); // 128-bit key
							inputList = new int[]{2,66,112,1123};
						} else if (conf.netConf.getMyId() == 1) {
							key = ByteArithmetic.toBoolean("000102030405060708090a0b0c0d0e0f"); // 128-bit key
							inputList = new int[]{1,3,66,1123};
						}
						
						PrivateSetDemo app = new PrivateSetDemo(conf.netConf.getMyId(), key, inputList);

						SCE sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
						sce.runApplication(app);

						boolean[][] actualBoolean = new boolean[app.result.length][app.result[0].length];
						
						for(int j = 0; j <app.result.length; j++){
							for (int i=0; i<app.result[0].length; i++) {
								actualBoolean[j][i] = app.result[j][i].getValue();
							}
							String actual = ByteArithmetic.toHex(actualBoolean[j]);
							result[j] = actual;
						}
					}
				};
			}
		};
		TestThreadRunner.run(f, conf);
		return result;
	}
	
	private ProtocolSuite getTinyTablesPreproProtocolSuite(int playerId) {
		return TinyTablesPreproProtocolSuite.getInstance(playerId);
	}

	private ProtocolSuiteConfiguration getTinyTablesPreproConfiguration(int myPort, int playerId){
		TinyTablesPreproConfiguration config = new TinyTablesPreproConfiguration();
		config.setTinyTablesFile(getTinyTablesFile(playerId));
		return config;
	}
	
	private ProtocolSuite getTinyTablesProtocolSuite(int playerId) {
		return TinyTablesProtocolSuite.getInstance(playerId);
	}

	private File getTinyTablesFile(int playerId) {
		String filename = "TinyTables_SetIntersection_" + playerId;
		return new File(filename);
	}
	
	private ProtocolSuiteConfiguration getTinyTablesConfiguration(int playerId) throws ClassNotFoundException, IOException {
		TinyTablesConfiguration config = new TinyTablesConfiguration();
		config.setTinyTablesFile(getTinyTablesFile(playerId));
		return config;
	}

}
