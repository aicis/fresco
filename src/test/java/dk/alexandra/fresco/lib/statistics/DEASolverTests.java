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
package dk.alexandra.fresco.lib.statistics;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import org.junit.Assert;

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
import dk.alexandra.fresco.lib.helper.AlgebraUtil;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.suite.spdz.utils.Util;

/**
 * Test class for the DEASolver.
 * Will generate a random data sample and perform a Data Envelopment
 * Analysis on it. The TestDEADSolver takes the size of the problem
 * as inputs (i.e. the number of input and output variables, the number
 * of rows in the basis and the number of queries to perform.
 * The MPC result is compared with the result of a plaintext DEA solver.    
 *
 */
public class DEASolverTests {

	private static final int BENCHMARKING_BIG_M = 1000000;
	
	private abstract static class ThreadWithFixture extends TestThread {

		protected SCE sce;

		@Override
		public void setUp() throws IOException {
			sce = SCEFactory.getSCEFromConfiguration(conf.sceConf, conf.protocolSuiteConf);
		}

	}

	public static class TestDEASolver extends TestThreadFactory {
		
		private int inputVariables;
		private int outputVariables;
		private int datasetRows;
		private int targetQueries;
		
		public TestDEASolver(int inputVariables, int outputVariables, int rows, int queries) {
			this.inputVariables = inputVariables;
			this.outputVariables = outputVariables;
			this.datasetRows = rows;
			this.targetQueries = queries;
		}

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new ThreadWithFixture() {
				@Override
				public void test() throws Exception {
					
					OInt[] outs = new OInt[targetQueries];
					double[] plainResult = new double[targetQueries];
					TestApplication app = new TestApplication() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(
								ProtocolFactory factory) {
							BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
							NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
							Random rand = new Random(2);
							
							SequentialProtocolProducer sseq = new SequentialProtocolProducer();

							BigInteger[][] rawBasisInputs = new BigInteger[datasetRows][inputVariables];
							AlgebraUtil.randomFill(rawBasisInputs, 9, rand);
							BigInteger[][] rawBasisOutputs = new BigInteger[datasetRows][outputVariables];
							AlgebraUtil.randomFill(rawBasisOutputs, 9, rand);
							
							SInt[][] basisInputs = ioBuilder.inputMatrix(rawBasisInputs, 1);
							SInt[][] basisOutputs = ioBuilder.inputMatrix(rawBasisOutputs, 1);
							
							BigInteger[][] rawTargetInputs = new BigInteger[targetQueries][inputVariables];
							AlgebraUtil.randomFill(rawTargetInputs, 9, rand);
							BigInteger[][] rawTargetOutputs = new BigInteger[targetQueries][outputVariables];
							AlgebraUtil.randomFill(rawTargetOutputs, 9, rand);
							
							SInt[][] targetInputs = ioBuilder.inputMatrix(rawTargetInputs, 2);
							SInt[][] targetOutputs = ioBuilder.inputMatrix(rawTargetOutputs, 2);

							sseq.append(ioBuilder.getProtocol());
							
							DEASolver solver = new DEASolver(AlgebraUtil.arrayToList(targetInputs),
									AlgebraUtil.arrayToList(targetOutputs),
									AlgebraUtil.arrayToList(basisInputs), 
									AlgebraUtil.arrayToList(basisOutputs));
							
							sseq.append(solver.prepareApplication(factory));
							for(int i = 0; i< outs.length; i++){
								outs[i] = ioBuilder.output(solver.getResult()[i]);
							}
							sseq.append(ioBuilder.getProtocol());
							// Solve the problem using a plaintext solver
							PlaintextDEASolver plainSolver = new PlaintextDEASolver();
							plainSolver.addBasis(rawBasisInputs, rawBasisOutputs);
							
							double[] plain = plainSolver.solve(rawTargetInputs, rawTargetOutputs);
							for(int i= 0; i< plain.length; i++){
								plainResult[i] = plain[i]; 
							}

							return sseq;
						}
					};
					long startTime = System.nanoTime();
					sce.runApplication(app);
					long endTime = System.nanoTime();
					System.out.println("============ Seq Time: "
							+ ((endTime - startTime) / 1000000));
					// Perform postprocessing and compare MPC result with plaintext result
					for(int i =0; i< outs.length; i++){
						Assert.assertEquals(plainResult[i], postProcess(outs[i]), 0.0000001);
					}
					
				}
			};
		}
	}

	/**
	 * Reduces a field-element to a double using Gauss reduction. 
	 */
	private static double postProcess(OInt input){
		BigInteger[] gauss = gauss(input.getValue());
		return (gauss[0].doubleValue()/gauss[1].doubleValue())-BENCHMARKING_BIG_M;
	}


	private static BigInteger[] gauss(BigInteger product) {
		BigInteger[] u = {Util.getModulus(),
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

	private static BigInteger innerproduct(BigInteger[] u, BigInteger[] v) {
		return u[0].multiply(v[0]).add(u[1].multiply(v[1]));
	}

}
