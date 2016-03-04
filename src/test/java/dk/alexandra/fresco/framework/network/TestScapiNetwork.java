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
package dk.alexandra.fresco.framework.network;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.junit.Test;

import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.TestConfiguration;
import edu.biu.scapi.comm.FasterTCPChannel.FasterMessage;

public class TestScapiNetwork {

	private abstract static class ThreadWithFixture extends TestThread {

		protected ScapiNetworkImpl network;
		protected int timeoutMillis = 10000;

		protected int noOfChannels() {
			return 1;
		}
		
		@Override
		public void setUp() {
			Reporter.init(Level.INFO);
			network = new ScapiNetworkImpl(conf.netConf, noOfChannels());
		}

	}
	
	private static void runTest(TestThreadFactory test, int n) {
		// Since SCAPI currently does not work with ports > 9999 we use fixed ports
		// here instead of relying on ephemeral ports which are often > 9999.
		List<Integer> ports = new ArrayList<Integer>(n);
		for (int i=1; i<=n; i++) {
			ports.add(9000 + i);
		}
		Map<Integer, NetworkConfiguration> netConf = TestConfiguration.getNetworkConfigurations(n, ports, Level.FINE);
		Map<Integer, TestThreadConfiguration> conf = new HashMap<Integer, TestThreadConfiguration>();
		for (int i : netConf.keySet()) {
			TestThreadConfiguration ttc = new TestThreadConfiguration();
			ttc.netConf = netConf.get(i);
			conf.put(i, ttc);
		}
		TestThreadRunner.run(test, conf);
		 
	}


	final TestThreadFactory test = new TestThreadFactory() {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					network.connect(timeoutMillis);
					network.close();
				}
			};
		}
	};

	
	@Test
	public void testCanConnect_2() throws Exception {
		runTest(test, 2);
	}
	
	@Test
	public void testCanConnect_3() throws Exception {
		runTest(test, 3);
	}

	@Test
	public void testCanConnect_7() throws Exception {
		runTest(test, 7);
	}

	
	
	@Test
	public void testPlayerTwoCanSendBytesToPlayerOne() throws Exception {
		final byte[] data = new byte[] { 0x42, 0xf, 0x00, 0x23, 0x15 };
		final TestThreadFactory test = new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new ThreadWithFixture() {
					@Override
					public void test() throws Exception {
						network.connect(timeoutMillis);
						if (conf.getMyId() == 1) {
							byte[] received = ((FasterMessage)network.receive(2)).getData();
							assertTrue(Arrays.equals(data, received ));
						} else if (conf.getMyId() == 2) {
							FasterMessage message = new FasterMessage(data);
							network.send(1, message);
						}
						network.close();
					}
				};
			}
		};
		runTest(test, 3);
	}

	

	@Test
	public void testCanUseDifferentChannels() throws Exception {
		 abstract class MyThreadWithFixture extends ThreadWithFixture {
			 protected int noOfChannels() {
				 return 2;
			 }
		}
			
		final byte[] data1 = new byte[] { 0x42, 0xf, 0x00, 0x23, 0x15 };
		final byte[] data2 = new byte[] { 0x34, 0x2, 0x00, 0x1, 0x22 };
		final TestThreadFactory test = new TestThreadFactory() {
			@Override
			public TestThread next(TestThreadConfiguration conf) {
				return new MyThreadWithFixture() {
					@Override
					public void test() throws Exception {
						network.connect(timeoutMillis);
						if (conf.getMyId() == 1) {
							network.send("0", 2, data2);
							byte[] received = (byte[])network.receive("1", 2);
							assertTrue(Arrays.equals(data1, received ));
						} else if (conf.getMyId() == 2) {
							network.send("1", 1, data1);
							byte[] received = (byte[])network.receive("0", 1);
							assertTrue(Arrays.equals(data2, received ));
						}
						network.close();
					}
				};
			}
		};
		runTest(test, 3);
	}
	
	
	
//	@Test
//	public void testPlayerOneAndTwoCanSwapBytes() throws Exception {
//		TestThreadRunner.run(new TestThreadFactory() {
//			@Override
//			public TestThread next(Configuration conf) {
//				return new ThreadWithFixture() {
//					@Override
//					public void test() throws Exception {
//						network.connect();
//						if (conf.getMyId() == 1) {
//							network.send(2, new byte[] { 0x42 });
//							network.flush();
//							byte[] data = new byte[1];
//							network.read(2, data);
//							assertEquals(0x44, data[0]);
//						} else if (conf.getMyId() == 2) {
//							network.send(1, new byte[] { 0x44 });
//							network.flush();
//							byte[] data = new byte[1];
//							network.read(1, data);
//							assertEquals(0x42, data[0]);
//						}
//					}
//				};
//			}
//		}, 3);
//	}
//
//	@Test
//	public void testCanSendToSelfAndReceiveLater() throws Exception {
//		TestThreadRunner.run(new TestThreadFactory() {
//			@Override
//			public TestThread next(Configuration conf) {
//				return new ThreadWithFixture() {
//					@Override
//					public void test() throws Exception {
//						network.connect();
//						byte[] data = new byte[] { (byte) conf.getMyId() };
//						network.send(conf.getMyId(), data);
//						network.flush();
//						byte[] received = new byte[1];
//						network.read(conf.getMyId(), received);
//						assertEquals((byte) conf.getMyId(), received[0]);
//					}
//				};
//			}
//		}, 3);
//	}
//	
//	@Test
//	public void testCanSendHugeDataAmounts() throws Exception{
//		TestThreadRunner.run(new TestThreadFactory() {
//			@Override
//			public TestThread next(Configuration conf) {
//				return new ThreadWithFixture(){
//					@Override
//					public void test() throws Exception {
//						network.connect();
//						byte[] data = new byte[32]; //size of a SheDoubleElement
//						int noOfSheDoubleElements = 100000;
//						if(conf.getMyId() == 1){
//							byte largeByte = (byte)255;						
//							for(int i = 0; i < data.length; i++)
//								data[i] = largeByte;
//							for(int i = 0; i < noOfSheDoubleElements; i++)
//								network.send(2, data);
//							network.flush();
//						}
//						if(conf.getMyId() == 2){
//							for(int i = 0; i < noOfSheDoubleElements; i++)
//								network.read(1, data);
//						}
//					}
//				};
//			}			
//		}, 3);
//	}
//	
//
//	// TODO: TEST deadlock gives timeout (should be able to specify timeout)
//	// TODO: SynchronousNetwork must have both timeout and maxbuf parameteres.

}
