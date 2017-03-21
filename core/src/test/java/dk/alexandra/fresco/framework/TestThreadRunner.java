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
package dk.alexandra.fresco.framework;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import com.esotericsoftware.minlog.Log;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import dk.alexandra.fresco.framework.network.KryoNetNetwork;
import dk.alexandra.fresco.framework.sce.SCE;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;

public class TestThreadRunner {

	private static final long MAX_WAIT_FOR_THREAD = 6000000;

	public abstract static class TestThread extends Thread {

		private boolean finished = false;

		protected TestThreadConfiguration conf;

		// Randomness to use in test.
		protected Random rand;
		
		protected Throwable setupException;

		protected Throwable testException;

		protected Throwable teardownException;
		
		protected SCE sce;

		void setConfiguration(TestThreadConfiguration conf) {
			this.conf = conf;
		}

		@Override
		public String toString() {
			return "TestThread(" + this.conf.netConf.getMyId() + ")";
		}
		
		@Override
		public void run() {
			try {
				//By default, only output warnings and worse for tests
				Level level = Level.WARNING;
				if(conf.sceConf != null && conf.sceConf.getLogLevel() != null) {
					level = conf.sceConf.getLogLevel();
				}				
				Reporter.init(level);
				if(conf.sceConf != null && conf.protocolSuiteConf != null) {
					sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
				}
				KryoNetNetwork.setLogLevel(Log.LEVEL_WARN);
				setUp();
				runTest();
			} catch (Throwable e) {
				Reporter.severe("" + this + " threw exception: ", e);
				this.setupException = e;
				Thread.currentThread().interrupt();
			} finally {
				runTearDown();
			}
		}

		private void runTest() {
			try {
				test();
			} catch (Exception e) {
				this.testException = e;
				Reporter.severe("" + this + " threw exception during test:", e);
				Thread.currentThread().interrupt();
			} catch (AssertionError e) {
				this.testException = e;
				Reporter.severe("Test assertion failed in " + this + ": ", e);
				Thread.currentThread().interrupt();
			}
		}

		private void runTearDown() {
			try {
				if(sce != null) {
					sce.shutdownSCE();
				}
				tearDown();
				finished = true;
			} catch (Exception e) {
				Reporter.severe("" + this + " threw exception during tear down:", e);
				this.teardownException = e;
				Thread.currentThread().interrupt();
			}
		}
		
		public void setUp() throws Exception {
			// Override this if test fixture setup needed.
		}

		public void tearDown() throws Exception {
			// Override this if actions needed to tear down test fixture.
		}

		public abstract void test() throws Exception;

		public void setRandom(long nextLong) {
			this.rand = new Random(nextLong);
			
		}

	}

	
	/**
	 * Container for all the configuration that one thread should have.
	 *
	 */
	public static class TestThreadConfiguration {

		public NetworkConfiguration netConf;
		public ProtocolSuiteConfiguration protocolSuiteConf;
		public SCEConfiguration sceConf;

		public int getMyId() {
			return this.netConf.getMyId();
		}

		public int getNoOfParties() {
			return this.netConf.noOfParties();
		}
		
	}
	

	public abstract static class TestThreadFactory {
		public abstract TestThread next(TestThreadConfiguration conf);
	}
	
	public static void run(TestThreadFactory f, int noOfPlayers) {
		int randSeed = 42;
		run(f, noOfPlayers, randSeed);
	}
	
	public static void run(TestThreadFactory f, int noOfPlayers, int randSeed) {
		Map<Integer, NetworkConfiguration> netConfs = TestConfiguration.getNetworkConfigurations(noOfPlayers, Level.FINE);
		
		Map<Integer, TestThreadConfiguration> confs = new HashMap<Integer, TestThreadConfiguration>();
		for (int i : netConfs.keySet()) {
			TestThreadConfiguration ttc = new TestThreadConfiguration();
			ttc.netConf = netConfs.get(i);
			confs.put(i, ttc);
		}
		run(f, confs, randSeed);
	}
	
	public static void run(TestThreadFactory f, Map<Integer, TestThreadConfiguration> confs) {
		int randSeed = 3457878;
		run(f, confs, randSeed);
	}
	
	public static void run(TestThreadFactory f, Map<Integer, TestThreadConfiguration> confs, int randSeed) throws TestFrameworkException {
		// TODO: Rather use thread container from util.concurrent?

		final Set<TestThread> threads = new HashSet<TestThread>();
		final int n = confs.size();

		Random r = new Random(randSeed);
		for (int i = 0; i < n; i++) {
			TestThreadConfiguration c = confs.get(i + 1);
			TestThread t = f.next(c);
			t.setConfiguration(c);
			t.setRandom(r.nextLong());
			threads.add(t);
		}

		for (Thread t : threads) {
			t.start();
		}

		for (TestThread t : threads) {
			try {
				t.join(MAX_WAIT_FOR_THREAD);
			} catch (InterruptedException e) {
				throw new TestFrameworkException("Test was interrupted");
			}
			if (!t.finished) {
				Reporter.severe("" + t + " timed out");
				throw new TestFrameworkException(t + " timed out");
			}
			if (t.setupException != null) {
				throw new TestFrameworkException(t + " threw exception in setup (see stderr)");
			} else if (t.testException != null) {
				throw new TestFrameworkException(t + " threw exception in test (see stderr)");
			} else if (t.teardownException != null) {
				throw new TestFrameworkException(t + " threw exception in teardown (see stderr)");
			}
		}
	}

}
