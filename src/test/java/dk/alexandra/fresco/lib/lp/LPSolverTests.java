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
package dk.alexandra.fresco.lib.lp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;

import org.junit.Assert;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.SCE;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementFactory;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import dk.alexandra.fresco.suite.spdz.utils.LPInputReader;
import dk.alexandra.fresco.suite.spdz.utils.PlainLPInputReader;
import dk.alexandra.fresco.suite.spdz.utils.PlainSpdzLPPrefix;

public class LPSolverTests {

	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
		}

	}

	public static class Container {
		private Object o;
		public void store(Object o) {
			this.o = o;
		}
		@SuppressWarnings("unchecked")
		public <T> T fetch(){
			return (T)o;
		}
	}
	
	public static class TestLPSolver extends TestThreadFactory {
		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					final Container c = new Container(); 
					OInt[] outs = new OInt[1];
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {
							BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
							LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
							NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
							ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
							PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
							RandomFieldElementFactory randFactory = (RandomFieldElementFactory) factory;
							LPFactory lpFactory = new LPFactoryImpl(80, bnFactory, localInvFactory, numericBitFactory, expFromOIntFactory, expFactory, randFactory);
							File pattern = new File("src/test/resources/lp/pattern7.csv");
							File program = new File("src/test/resources/lp/program7.csv");
							LPInputReader inputreader = null;
							try {
								inputreader = PlainLPInputReader
										.getFileInputReader(program, pattern,
												conf.getMyId());
							} catch (FileNotFoundException e) {
								e.printStackTrace();
								throw new MPCException(
										"Could not read needed files: "
												+ e.getMessage(), e);
							}
							SequentialProtocolProducer sseq = new SequentialProtocolProducer();
							for (int i = 0; i < 1; i++) {
								LPPrefix prefix = null;
								try {
									prefix = new PlainSpdzLPPrefix(inputreader,
											bnFactory);
								} catch (IOException e) {
									e.printStackTrace();
									throw new MPCException("IOException: "
											+ e.getMessage(), e);
								}
								ProtocolProducer lpsolver = new LPSolverProtocol(
										prefix.getTableau(),
										prefix.getUpdateMatrix(),
										prefix.getPivot(), 
										prefix.getLambdas(), 
										lpFactory, bnFactory);
								SInt sout = bnFactory.getSInt();
								OInt out = bnFactory.getOInt();
								outs[0] = out;
								ProtocolProducer outputter = lpFactory
										.getOptimalValueProtocol(
												prefix.getUpdateMatrix(),
												prefix.getTableau().getB(),
												prefix.getPivot(), sout);
								ProtocolProducer open = bnFactory
										.getOpenProtocol(sout, out);
								
								OmniBuilder builder = new OmniBuilder(factory);
								OInt[] lambdas = builder.getNumericIOBuilder().outputArray(prefix.getLambdas());
								c.store(lambdas);
								
								ProtocolProducer seq = new SequentialProtocolProducer(
										prefix.getPrefix(), 
										lpsolver,
										outputter, 
										open,
										builder.getProtocol());
								sseq.append(seq);
							}
							return sseq;
						}
					};
					long startTime = System.nanoTime();
					sce.runApplication(app);
					long endTime = System.nanoTime();
					System.out.println("============ Seq Time: "
							+ ((endTime - startTime) / 1000000));
					//TODO: Ensure that this is indeed the correct result. 
					Assert.assertEquals(new BigInteger("161"), outs[0].getValue());
					//TODO: Check lambdas					
				}
			};
		}
	}
/*
	private String printMatrix(Matrix<SInt> matrix, String label, SCE sce,
			Spdzfactory factory) {
		SInt[][] C = matrix.getDoubleArray();
		OInt[][] COut = Util
				.oIntFill(new OInt[C.length][C[0].length], factory);
		ProtocolProducer output = Util.makeOpenCircuit(C, COut, factory);
		sce.runApplication(output);
		return printBigIntegers(COut, label);
	}

	private String printBigIntegers(OInt[][] matrix, String label) {
		int maxlength = 0;
		String[][] strings = new String[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				strings[i][j] = matrix[i][j].getValue().toString();
				int length = strings[i][j].length();
				if (length > maxlength) {
					maxlength = length;
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append(label + "\n");
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				int length = strings[i][j].length();
				for (int k = 0; k < maxlength + 3 - length; k++) {
					sb.append(' ');
				}
				sb.append(strings[i][j]);
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	private BigInteger[] gauss(BigInteger product) {
		BigInteger[] u = { mpc.runtime.spdz.utils.Util.getModulus(),
				BigInteger.ZERO };
		BigInteger[] v = { product, BigInteger.ONE };
		BigInteger two = BigInteger.valueOf(2);
		BigInteger lenU = innerproduct(u, u);
		BigInteger lenV = innerproduct(v, v);
		if (lenU.compareTo(lenV) < 0) {
			BigInteger[] temp = u;
			u = v;
			v = temp;
		}
		do {
			BigInteger uv = innerproduct(u, v);
			BigInteger[] q = uv.divideAndRemainder(innerproduct(v, v));
			if (uv.compareTo(q[1].divide(two)) < 0) {
				q[0] = q[0].add(BigInteger.ONE);
			}
			BigInteger r0 = u[0].subtract(v[0].multiply(q[0]));
			BigInteger r1 = u[1].subtract(v[1].multiply(q[0]));
			u = v;
			v = new BigInteger[] { r0, r1 };
			lenU = innerproduct(u, u);
			lenV = innerproduct(v, v);
		} while (lenU.compareTo(lenV) > 0);
		return u;
	}

	private BigInteger innerproduct(BigInteger[] u, BigInteger[] v) {
		return u[0].multiply(v[0]).add(u[1].multiply(v[1]));
	}
	*/
}
