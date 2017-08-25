/*
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

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderHelper;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AlgebraUtil;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.statistics.DEASolver.AnalysisType;
import dk.alexandra.fresco.lib.statistics.DEASolver.DEAResult;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Assert;

/**
 * Test class for the DEASolver.
 * Will generate a random data sample and perform a Data Envelopment
 * Analysis on it. The TestDEADSolver takes the size of the problem
 * as inputs (i.e. the number of input and output variables, the number
 * of rows in the basis and the number of queries to perform.
 * The MPC result is compared with the result of a plaintext DEA solver.
 */
public class DEASolverTests {

  public static class TestDEASolver<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private int inputVariables;
    private int outputVariables;
    private int datasetRows;
    private int targetQueries;
    private AnalysisType type;
    private BasicNumericFactory bnFactory;

    public TestDEASolver(int inputVariables, int outputVariables, int rows, int queries,
        AnalysisType type) {
      this.inputVariables = inputVariables;
      this.outputVariables = outputVariables;
      this.datasetRows = rows;
      this.targetQueries = queries;
      this.type = type;
    }

    @Override
    public TestThread next(TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        private BigInteger[][] rawTargetOutputs;
        private BigInteger[][] rawTargetInputs;
        private BigInteger[][] rawBasisOutputs;
        private BigInteger[][] rawBasisInputs;
        private List<List<SInt>> setOutput;
        private List<List<SInt>> setInput;
        private List<List<SInt>> outputValues;
        private List<List<SInt>> inputValues;

        @Override
        public void test() throws Exception {
          long startTime = System.nanoTime();

          List<Computation<BigInteger>> outs = new ArrayList<>(targetQueries);
          List<List<Computation<BigInteger>>> basis = new ArrayList<>(targetQueries);
          double[] plainResult = new double[targetQueries];
          Application<Void, ProtocolBuilderNumeric> app = new Application<Void, ProtocolBuilderNumeric>() {

            @Override
            public Computation<Void> prepareApplication(ProtocolBuilderNumeric producer) {
              producer
                  .append(prepareApplication(ProtocolBuilderHelper.getFactoryNumeric(producer)));
              return () -> null;
            }

            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory provider = factoryProducer.getProtocolFactory();
              bnFactory = (BasicNumericFactory) provider;
              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              Random rand = new Random(2);

              SequentialProtocolProducer sseq = new SequentialProtocolProducer();

              rawBasisInputs = new BigInteger[datasetRows][inputVariables];
              AlgebraUtil.randomFill(rawBasisInputs, 9, rand);
              rawBasisOutputs = new BigInteger[datasetRows][outputVariables];
              AlgebraUtil.randomFill(rawBasisOutputs, 9, rand);

              SInt[][] basisInputs = ioBuilder.inputMatrix(rawBasisInputs, 1);
              SInt[][] basisOutputs = ioBuilder.inputMatrix(rawBasisOutputs, 1);

              rawTargetInputs = new BigInteger[targetQueries][inputVariables];
              AlgebraUtil.randomFill(rawTargetInputs, 9, rand);
              rawTargetOutputs = new BigInteger[targetQueries][outputVariables];
              AlgebraUtil.randomFill(rawTargetOutputs, 9, rand);

              SInt[][] targetInputs = ioBuilder.inputMatrix(rawTargetInputs, 2);
              SInt[][] targetOutputs = ioBuilder.inputMatrix(rawTargetOutputs, 2);

              sseq.append(ioBuilder.getProtocol());

              inputValues = AlgebraUtil.arrayToList(targetInputs);
              outputValues = AlgebraUtil.arrayToList(targetOutputs);
              setInput = AlgebraUtil.arrayToList(basisInputs);
              setOutput = AlgebraUtil.arrayToList(basisOutputs);
              return sseq;
            }
          };
          ResourcePoolT resourcePool = ResourcePoolCreator.createResourcePool(conf.sceConf);
          secureComputationEngine.runApplication(app, resourcePool);

          DEASolver solver = new DEASolver(type, inputValues,
              outputValues,
              setInput,
              setOutput);

          List<DEAResult> deaResults = secureComputationEngine.runApplication(solver, resourcePool);

          Application<Void, ProtocolBuilderNumeric> app2 = new Application<Void, ProtocolBuilderNumeric>() {

            @Override
            public Computation<Void> prepareApplication(ProtocolBuilderNumeric producer) {
              producer
                  .append(prepareApplication(ProtocolBuilderHelper.getFactoryNumeric(producer)));
              return () -> null;
            }

            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory provider = factoryProducer.getProtocolFactory();
              bnFactory = (BasicNumericFactory) provider;
              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              SequentialProtocolProducer sseq = new SequentialProtocolProducer();

              for (DEAResult deaResult : deaResults) {
                outs.add(ioBuilder.output(deaResult.optimal));
                basis.add(ioBuilder.outputArray(deaResult.basis.toArray(new SInt[0])));
              }

              sseq.append(ioBuilder.getProtocol());
              // Solve the problem using a plaintext solver
              PlaintextDEASolver plainSolver = new PlaintextDEASolver();
              plainSolver.addBasis(rawBasisInputs, rawBasisOutputs);

              double[] plain = plainSolver.solve(rawTargetInputs, rawTargetOutputs, type);
              System.arraycopy(plain, 0, plainResult, 0, plain.length);

              return sseq;
            }
          };
          secureComputationEngine.runApplication(app2, resourcePool);

          long endTime = System.nanoTime();
          System.out.println("============ Seq Time: "
              + ((endTime - startTime) / 1000000));
          // Perform postprocessing and compare MPC result with plaintext result
          int lambdas = datasetRows;

          int constraints = inputVariables + outputVariables + 1;
          int slackvariables = constraints;
          int variables = lambdas + slackvariables + 1 + 2; //+2 is new

          for (int i = 0; i < targetQueries; i++) {          
            Assert.assertEquals(plainResult[i],
                postProcess(outs.get(i).out(), type, bnFactory.getModulus()), 0.0000001);
            for (int j = 0; j < basis.get(i).size(); j++) {
              int value = basis.get(i).get(j).out().intValue();
              Assert.assertTrue(
                  "Basis value " + value + ", was larger than " + (
                      variables - 1), value < variables);
            }          
          }
        }
      };
    }
  }

  /**
   * Reduces a field-element to a double using Gauss reduction.
   */
  private static double postProcess(BigInteger input, AnalysisType type,
      BigInteger modulus) {
    BigInteger[] gauss = gauss(input, modulus);
    double res = (gauss[0].doubleValue() / gauss[1].doubleValue());
    if (type == DEASolver.AnalysisType.INPUT_EFFICIENCY) {
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
   * @param product The integer <i>t = r*s<sup>-1</sup>mod N</i>. Note that we must have that
   * <i>2r*s < N</i>.
   * @param mod the modulus, i.e., <i>N</i>.
   * @return The fraction as represented as the rational number <i>r/s</i>.
   */
  private static BigInteger[] gauss(BigInteger product, BigInteger mod) {
    product = product.mod(mod);
    BigInteger[] u = {mod, BigInteger.ZERO};
    BigInteger[] v = {product, BigInteger.ONE};
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
      v = new BigInteger[]{r0, r1};
      uu = vv;
      uv = innerproduct(u, v);
      vv = innerproduct(v, v);
    } while (uu.compareTo(vv) > 0);
    return new BigInteger[]{u[0], u[1]};
  }

  private static BigInteger innerproduct(BigInteger[] u, BigInteger[] v) {
    return u[0].multiply(v[0]).add(u[1].multiply(v[1]));
  }

}
