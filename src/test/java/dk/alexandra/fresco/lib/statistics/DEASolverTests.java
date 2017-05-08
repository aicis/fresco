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

import java.math.BigInteger;
import java.util.Random;

import org.junit.Assert;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
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

	public static class TestDEASolver extends TestThreadFactory {
		
		private int inputVariables;
		private int outputVariables;
		private int datasetRows;
		private int targetQueries;
		private DEASolver.AnalysisType type;
		
		public TestDEASolver(int inputVariables, int outputVariables, int rows, int queries, DEASolver.AnalysisType type) {
			this.inputVariables = inputVariables;
			this.outputVariables = outputVariables;
			this.datasetRows = rows;
			this.targetQueries = queries;
			this.type = type;
		}

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new TestThread() {
				@Override
				public void test() throws Exception {
					
					OInt[] outs = new OInt[targetQueries];
					OInt[][] basis = new OInt[targetQueries][];
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
							
							DEASolver solver = new DEASolver(type, AlgebraUtil.arrayToList(targetInputs),
									AlgebraUtil.arrayToList(targetOutputs),
									AlgebraUtil.arrayToList(basisInputs), 
									AlgebraUtil.arrayToList(basisOutputs));
							
							sseq.append(solver.prepareApplication(factory));
							for(int i = 0; i< outs.length; i++){
								outs[i] = ioBuilder.output(solver.getResult()[i]);
							}
							
							for(int i = 0; i< basis.length; i++){
								SInt[] bs = solver.getBasis()[i];
								basis[i] = ioBuilder.outputArray(bs);
							}
							
							sseq.append(ioBuilder.getProtocol());
							// Solve the problem using a plaintext solver
							PlaintextDEASolver plainSolver = new PlaintextDEASolver();
							plainSolver.addBasis(rawBasisInputs, rawBasisOutputs);
							
							double[] plain = plainSolver.solve(rawTargetInputs, rawTargetOutputs, type);
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
					int lambdas = datasetRows;
					int constraints = inputVariables + outputVariables + 1;
					int slackvariables = constraints;
					int variables = lambdas + slackvariables + 1;
					System.out.println("variables:" + variables);
					for(int i =0; i< outs.length; i++){
						Assert.assertEquals(plainResult[i], postProcess(outs[i], type), 0.0000001);
						System.out.println("DEA Score (plain result): " + plainResult[i]);
						System.out.println("DEA Score (solver result): " + postProcess(outs[i], type));
//						System.out.println("Final Basis for request no. "+i+" is: "+Arrays.toString(basis[i]));						
						for(int j = 0; j < basis[i].length; j++) {
							Assert.assertTrue("Basis value "+basis[i][j].getValue().intValue()+", was larger than "+(variables-1), basis[i][j].getValue().intValue() < variables );
						}
					}
					//Determine which lambda's should be included
					
					
				}
			};
		}
	}

	/**
	 * Reduces a field-element to a double using Gauss reduction. 
	 */
	private static double postProcess(OInt input, DEASolver.AnalysisType type){
		BigInteger[] gauss = gauss(input.getValue(), Util.getModulus());
		double res = (gauss[0].doubleValue()/gauss[1].doubleValue());
		if(type == DEASolver.AnalysisType.OUTPUT_EFFICIENCY) {
			res -= BENCHMARKING_BIG_M;
		} else {
			res *= -1;
		}
		return res;
	}

	/**
	 * Converts a number of the form <i>t = r*s<sup>-1</sup> mod N</i> to the
	 * rational number <i>r/s</i> represented as a reduced fraction.
	 * <p>
	 * This is useful outputting non-integer rational numbers from MPC, when
	 * outputting a non-reduced fraction may leak too much information. The
	 * technique used is adapted from the paper "CryptoComputing With Rationals"
	 * of Fouque et al. Financial Cryptography 2002. This methods restricts us
	 * to integers <i>t = r*s<sup>-1</sup> mod N</i> so that <i>2r*s < N</i>.
	 * See
	 * <a href="https://www.di.ens.fr/~stern/data/St100.pdf">https://www.di.ens.
	 * fr/~stern/data/St100.pdf</a>
	 * </p>
	 * 
	 * @param product
	 *            The integer <i>t = r*s<sup>-1</sup>mod N</i>. Note that we
	 *            must have that <i>2r*s < N</i>.
	 *
	 * @param mod
	 *            the modulus, i.e., <i>N</i>.
	 * 
	 * @return The fraction as represented as the rational number <i>r/s</i>.
	 */
	static BigInteger[] gauss(BigInteger product, BigInteger mod) {
		product = product.mod(mod);
		BigInteger[] u = { mod, BigInteger.ZERO };
		BigInteger[] v = { product, BigInteger.ONE };
		BigInteger two = BigInteger.valueOf(2);
		BigInteger uv = innerproduct(u, v);
		BigInteger vv = innerproduct(v, v);
		BigInteger uu = innerproduct(u, u);
		do {
			BigInteger[] q = uv.divideAndRemainder(vv);
			boolean negRes = q[1].signum() == -1;
			if (!negRes) {
				if (vv.compareTo(q[1].multiply(two)) <= 0) {
					q[0] = q[0].add(BigInteger.ONE);
				}
			} else {
				if (vv.compareTo(q[1].multiply(two.negate())) <= 0) {
					q[0] = q[0].subtract(BigInteger.ONE);
				}
			}
			BigInteger r0 = u[0].subtract(v[0].multiply(q[0]));
			BigInteger r1 = u[1].subtract(v[1].multiply(q[0]));
			u = v;
			v = new BigInteger[] { r0, r1 };
			uu = vv;
			uv = innerproduct(u, v);
			vv = innerproduct(v, v);
		} while (uu.compareTo(vv) > 0);
		return new BigInteger[] { u[0], u[1] };
	}

	private static BigInteger innerproduct(BigInteger[] u, BigInteger[] v) {
		return u[0].multiply(v[0]).add(u[1].multiply(v[1]));
	}

}
