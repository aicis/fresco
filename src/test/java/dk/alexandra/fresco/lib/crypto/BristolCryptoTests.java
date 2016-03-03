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
package dk.alexandra.fresco.lib.crypto;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.SCE;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.bristol.BristolCircuit;
import dk.alexandra.fresco.lib.helper.builder.LogicBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

/**
 * Some generic tests for basic crypto primitives a la AES and SHA1.
 * 
 * Can be used to test any protocol suite that supports BasicLogicFactory.
 *
 */
public class BristolCryptoTests {
	
	private abstract static class ThreadWithFixture extends TestThread {


		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);				
		}

	}
	

	/**
	 * Convert hex string to boolean array.
	 * 
	 * 	// 1 --> true, 0 --> false
	 * 
	 */
	private static boolean[] toBoolean(String hex) throws IllegalArgumentException {
		if (hex.length() % 2 != 0)
			throw new IllegalArgumentException("Illegal hex string");
		boolean[] res = new boolean[hex.length() * 4];
		for (int i=0; i<hex.length() / 2; i++) {
			String sub = hex.substring(2*i,2*i +2);
			int value = Integer.parseInt(sub, 16);
			int numOfBits = 8;
			for (int j = 0; j < numOfBits; j++) {
				boolean val = (value & 1 << j) != 0;
		        res[8*i + (numOfBits - j - 1)] = val;
		    }
		}
		return res;
	}
	
	
	@Test
	public void testToBoolean() throws Exception {
		boolean[] res = toBoolean("2b7e151628aed2a6abf7158809cf4f3c");
		assertTrue(
				Arrays.equals(new boolean[] { false, false, true, false, true, false, true, true, false, true, true, true }, Arrays.copyOf(res, 12)));
	}
	
	
	/**
	 * Testing AES encryption using standard test vectors.
	 * 
	 * TODO: Include more FIPS test vectors.
	 *
	 */
	public static class AesTest extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				
				// This is just some fixed test vectors for AES in ECB mode that was
				// found somewhere on the net, i.e., this is some known plaintexts and
				// corresponding cipher texts that can be used for testing.
				final String[] keyVec = new String[] { "000102030405060708090a0b0c0d0e0f"};
				final String plainVec = "00112233445566778899aabbccddeeff";
				final String[] cipherVec = new String[] { "69c4e0d86a7b0430d8cdb78070b4c55a"};
				
				SBool[] plain, key, cipher;
				OBool[] openedCipher;
				
				@Override
				public void test() throws Exception {
					Application aesApp = new Application() {

						private static final long serialVersionUID = 1923498347L;

						@Override
						public ProtocolProducer prepareApplication(ProtocolFactory fac) {
							BasicLogicFactory bool = (BasicLogicFactory)fac;
							LogicBuilder builder = new LogicBuilder(bool);
							
							boolean[] key_val = toBoolean(keyVec[0]);
							boolean[] in_val = toBoolean(plainVec);
							
							plain = builder.input(1, in_val);
							key = builder.input(1, key_val);
							cipher = bool.getSBools(128);

							// Create AES circuit.
							BristolCryptoFactory aesFac = new BristolCryptoFactory(bool);
							BristolCircuit aes = aesFac.getAesCircuit(plain, key, cipher);
							builder.addGateProducer(aes);
							
							// Create circuits for opening result of AES.							
							openedCipher = builder.output(cipher);
							
							return new SequentialProtocolProducer(builder.getCircuit());
						}
					};

					sce.runApplication(aesApp);

					boolean[] expected = toBoolean(cipherVec[0]);
					boolean[] actual = new boolean[128];
					for (int i=0; i<128; i++) {
						actual[i] = openedCipher[i].getValue();
					}
					
					//					System.out.println("KEY       : " + Arrays.toString(toBoolean(keyVec)));
					//					System.out.println("IN        : " + Arrays.toString(toBoolean(inVec[0])));
					//					System.out.println("EXPECTED  : " + Arrays.toString(expected));
					//					System.out.println("ACTUAL OPN: " + Arrays.toString(actual));
					
					Assert.assertTrue(Arrays.equals(expected, actual));
					
				}
			};
		}
	}
	
	

	/**
	 * Testing SHA-1 compression function.
	 * 
	 * TODO: Include all three test vectors.
	 *
	 */
	public static class Sha1Test extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				SBool[] in, out;
				OBool[] openedOut;
				
				/*
				 * IMPORTANT: These are NOT test vectors for the complete SHA-1
				 * hash function, as the padding rules are ignored. Therefore,
				 * use of tools like md5sum will produce a different output if
				 * supplied with the same inputs.
				 */
				String in1 ="00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
				String out1 = "92b404e556588ced6c1acd4ebf053f6809f73a93";
				String in2 = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f";
				String out2 = "b9ac757bbc2979252e22727406872f94cbea56a1";
				String in3 = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
				String out3 = "bafbc2c87c33322603f38e06c3e0f79c1f1b1475";
				
				@Override
				public void test() throws Exception {
					Application aesApp = new Application() {

						private static final long serialVersionUID = 984759485L;

						@Override
						public ProtocolProducer prepareApplication(ProtocolFactory fac) {
							BasicLogicFactory bool = (BasicLogicFactory)fac;

							boolean[] in_val = toBoolean(in1);
							in = bool.getKnownConstantSBools(in_val);
							out = bool.getSBools(160);

							// Create SHA1 circuit.
							BristolCryptoFactory sha1Fac = new BristolCryptoFactory(bool);
							BristolCircuit aes = sha1Fac.getSha1Circuit(in, out);
							
							// Create circuits for opening result of AES.
							ProtocolProducer[] opens = new ProtocolProducer[out.length];
							openedOut = new OBool[out.length];
							for (int i=0; i<out.length; i++) {
								openedOut[i] = bool.getOBool();
								opens[i] = bool.getOpenProtocol(out[i], openedOut[i]);
							}
							ProtocolProducer open_all = new ParallelProtocolProducer(opens);
							
							return new SequentialProtocolProducer(aes, open_all);
						}
					};

					sce.runApplication(aesApp);

					boolean[] expected = toBoolean(out1);
					boolean[] actual = new boolean[out.length];
					for (int i=0; i<out.length; i++) {
						actual[i] = openedOut[i].getValue();
					}

					//					System.out.println("IN        : " + Arrays.toString(AesTests.toBoolean(in1)));
					//					System.out.println("EXPECTED  : " + Arrays.toString(expected));
					//					System.out.println("ACTUAL    : " + Arrays.toString(actual));
					
					Assert.assertTrue(Arrays.equals(expected, actual));
					
				}
			};
		}
	}
	
	
	


	/**
	 * Testing SHA-1 compression function.
	 * 
	 * TODO: Include all three test vectors.
	 *
	 */
	public static class Sha256Test extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				SBool[] in, out;
				OBool[] openedOut;
				
				/*
				 * IMPORTANT: These are NOT test vectors for the complete SHA-256
				 * hash function, as the padding rules are ignored. Therefore,
				 * use of tools like md5sum will produce a different output if
				 * supplied with the same inputs.
				 */
				String in1 ="00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
				String out1 = "da5698be17b9b46962335799779fbeca8ce5d491c0d26243bafef9ea1837a9d8";
				String in2 = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f";
				String out2 = "fc99a2df88f42a7a7bb9d18033cdc6a20256755f9d5b9a5044a9cc315abe84a7";
				String in3 = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
				String out3 = "ef0c748df4da50a8d6c43c013edc3ce76c9d9fa9a1458ade56eb86c0a64492d2";
				String in4 = "243f6a8885a308d313198a2e03707344a4093822299f31d0082efa98ec4e6c89452821e638d01377be5466cf34e90c6cc0ac29b7c97c50dd3f84d5b5b5470917";
				String out4 = "cf0ae4eb67d38ffeb94068984b22abde4e92bc548d14585e48dca8882d7b09ce";
				
				@Override
				public void test() throws Exception {
					Application sha256App = new Application() {

						private static final long serialVersionUID = 984759485L;

						@Override
						public ProtocolProducer prepareApplication(ProtocolFactory fac) {
							BasicLogicFactory bool = (BasicLogicFactory)fac;

							boolean[] in_val = toBoolean(in1);
							in = bool.getKnownConstantSBools(in_val);
							out = bool.getSBools(256);

							// Create SHA1 circuit.
							BristolCryptoFactory sha256Fac = new BristolCryptoFactory(bool);
							BristolCircuit sha256 = sha256Fac.getSha256Circuit(in, out);
							
							// Create circuits for opening result of SHA 256.
							ProtocolProducer[] opens = new ProtocolProducer[out.length];
							openedOut = new OBool[out.length];
							for (int i=0; i<out.length; i++) {
								openedOut[i] = bool.getOBool();
								opens[i] = bool.getOpenProtocol(out[i], openedOut[i]);
							}
							ProtocolProducer open_all = new ParallelProtocolProducer(opens);
							
							return new SequentialProtocolProducer(sha256, open_all);
						}
					};

					sce.runApplication(sha256App);

					boolean[] expected = toBoolean(out1);
					boolean[] actual = new boolean[out.length];
					for (int i=0; i<out.length; i++) {
						actual[i] = openedOut[i].getValue();
					}

					//					System.out.println("IN        : " + Arrays.toString(AesTests.toBoolean(in1)));
					//					System.out.println("EXPECTED  : " + Arrays.toString(expected));
					//					System.out.println("ACTUAL    : " + Arrays.toString(actual));
					
					Assert.assertTrue(Arrays.equals(expected, actual));
					
				}
			};
		}
	}
	
	
	/**
	 * TestingMD5 compression function.
	 * 
	 * TODO: Include all three test vectors.
	 *
	 */
	public static class MD5Test extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				SBool[] in, out;
				OBool[] openedOut;
				
				/*
				 * IMPORTANT: These are NOT test vectors for the complete SHA-1
				 * hash function, as the padding rules are ignored. Therefore,
				 * use of tools like md5sum will produce a different output if
				 * supplied with the same inputs.
				 */
				String in1 ="00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
				String out1 = "ac1d1f03d08ea56eb767ab1f91773174";
				String in2 = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f";
				String out2 = "cad94491c9e401d9385bfc721ef55f62";
				String in3 = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
				String out3 = "b487195651913e494b55c6bddf405c01";
				String in4 = "243f6a8885a308d313198a2e03707344a4093822299f31d0082efa98ec4e6c89452821e638d01377be5466cf34e90c6cc0ac29b7c97c50dd3f84d5b5b5470917";
				String out4 = "3715f568f422db75cc8d65e11764ff01";
				
				@Override
				public void test() throws Exception {
					Application md5App = new Application() {

						private static final long serialVersionUID = 984759485L;

						@Override
						public ProtocolProducer prepareApplication(ProtocolFactory fac) {
							BasicLogicFactory bool = (BasicLogicFactory)fac;

							boolean[] in_val = toBoolean(in1);
							in = bool.getKnownConstantSBools(in_val);
							out = bool.getSBools(128);

							// Create MD5 circuit.
							BristolCryptoFactory md5Fac = new BristolCryptoFactory(bool);
							BristolCircuit md5 = md5Fac.getMD5Circuit(in, out);
							
							// Create circuits for opening result of MD5.
							ProtocolProducer[] opens = new ProtocolProducer[out.length];
							openedOut = new OBool[out.length];
							for (int i=0; i<out.length; i++) {
								openedOut[i] = bool.getOBool();
								opens[i] = bool.getOpenProtocol(out[i], openedOut[i]);
							}
							ProtocolProducer open_all = new ParallelProtocolProducer(opens);
							
							return new SequentialProtocolProducer(md5, open_all);
						}
					};

					sce.runApplication(md5App);

					boolean[] expected = toBoolean(out1);
					boolean[] actual = new boolean[out.length];
					for (int i=0; i<out.length; i++) {
						actual[i] = openedOut[i].getValue();
					}

					//					System.out.println("IN        : " + Arrays.toString(AesTests.toBoolean(in1)));
					//					System.out.println("EXPECTED  : " + Arrays.toString(expected));
					//					System.out.println("ACTUAL    : " + Arrays.toString(actual));
					
					Assert.assertTrue(Arrays.equals(expected, actual));
					
				}
			};
		}
	}
	
	

	/**
	 * Testing circuit for mult of two 32-bit numbers.
	 * 
	 * TODO: Include more test vectors. Oddly enough, 1x1=2 :-)
	 *
	 */
	public static class Mult32x32Test extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				SBool[] in1, in2, out;
				OBool[] openedOut;
				
				String inv1 = "00000000";
				String inv2 = "00000000";
				String outv = "0000000000000000";
				@Override
				public void test() throws Exception {
					Application multApp = new Application() {

						private static final long serialVersionUID = 36363636L;

						@Override
						public ProtocolProducer prepareApplication(ProtocolFactory fac) {
							BasicLogicFactory bool = (BasicLogicFactory)fac;

							boolean[] in1_val = toBoolean(inv1);
							in1 = bool.getKnownConstantSBools(in1_val);
							boolean[] in2_val = toBoolean(inv2);
							in2 = bool.getKnownConstantSBools(in2_val);
							out = bool.getSBools(64);

							// Create mult circuit.
							BristolCryptoFactory multFac = new BristolCryptoFactory(bool);
							BristolCircuit mult = multFac.getMult32x32Circuit(in1, in2, out);
							
							// Create circuits for opening result of 32x32 bit mult.
							ProtocolProducer[] opens = new ProtocolProducer[out.length];
							openedOut = new OBool[out.length];
							for (int i=0; i<out.length; i++) {
								openedOut[i] = bool.getOBool();
								opens[i] = bool.getOpenProtocol(out[i], openedOut[i]);
							}
							ProtocolProducer open_all = new ParallelProtocolProducer(opens);
							
							return new SequentialProtocolProducer(mult, open_all);
						}
					};

					sce.runApplication(multApp);

					boolean[] expected = toBoolean(outv);
					boolean[] actual = new boolean[out.length];
					for (int i=0; i<out.length; i++) {
						actual[i] = openedOut[i].getValue();
					}

					//					System.out.println("IN1        : " + Arrays.toString(toBoolean(inv1)));
					//					System.out.println("IN2        : " + Arrays.toString(toBoolean(inv2)));
					//					System.out.println("EXPECTED   : " + Arrays.toString(expected));
					//					System.out.println("ACTUAL     : " + Arrays.toString(actual));
					
					Assert.assertTrue(Arrays.equals(expected, actual));
					
				}
			};
		}
	}
	
	
	/**
	 * Testing circuit for DES encryption.
	 * 
	 * TODO: Include more test vectors, e.g., from here:
	 * https://dl.dropboxusercontent.com/u/25980826/des.test
	 *
	 */
	public static class DesTest extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				SBool[] plain, key, cipher;
				OBool[] openedOut;
				
				String keyV = "0101010101010101";
				String plainV = "8000000000000000";
				String cipherV = "95F8A5E5DD31D900".toLowerCase();
				@Override
				public void test() throws Exception {
					Application md5App = new Application() {

						private static final long serialVersionUID = 36625566;

						@Override
						public ProtocolProducer prepareApplication(ProtocolFactory fac) {
							BasicLogicFactory bool = (BasicLogicFactory)fac;

							boolean[] in1_val = toBoolean(plainV);
							plain = bool.getKnownConstantSBools(in1_val);
							boolean[] in2_val = toBoolean(keyV);
							key = bool.getKnownConstantSBools(in2_val);
							cipher = bool.getSBools(64);

							// Create des circuit.
							BristolCryptoFactory desFac = new BristolCryptoFactory(bool);
							BristolCircuit des = desFac.getDesCircuit(plain, key, cipher);
							
							// Create circuits for opening result of DES.
							ProtocolProducer[] opens = new ProtocolProducer[cipher.length];
							openedOut = new OBool[cipher.length];
							for (int i=0; i<cipher.length; i++) {
								openedOut[i] = bool.getOBool();
								opens[i] = bool.getOpenProtocol(cipher[i], openedOut[i]);
							}
							ProtocolProducer open_all = new ParallelProtocolProducer(opens);
							
							return new SequentialProtocolProducer(des, open_all);
						}
					};

					sce.runApplication(md5App);

					boolean[] expected = toBoolean(cipherV);
					boolean[] actual = new boolean[cipher.length];
					for (int i=0; i<cipher.length; i++) {
						actual[i] = openedOut[i].getValue();
					}

					//					System.out.println("IN1        : " + Arrays.toString(toBoolean(inv1)));
					//					System.out.println("IN2        : " + Arrays.toString(toBoolean(inv2)));
					//					System.out.println("EXPECTED   : " + Arrays.toString(expected));
					//					System.out.println("ACTUAL     : " + Arrays.toString(actual));
					
					Assert.assertTrue(Arrays.equals(expected, actual));
					
				}
			};
		}
	}
	
}
