/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.FactoryProducer;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AlgebraUtil;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.statistics.DEASolver.AnalysisType;
import java.math.BigInteger;
import org.junit.Assert;

/**
 * Test class for the DEASolver.
 */
public class DEASolverFixedDataTest {

  private static final int BENCHMARKING_BIG_M = 1000000;

  private static int[][] dataSet1 = new int[][]{
      new int[]{29, 13451, 14409, 16477}, // Score 1
      new int[]{2, 581, 531, 1037}, // Score 1
      new int[]{26, 13352, 1753, 13528}, // Score 1
      new int[]{15, 4828, 949, 5126}, // Score 0.9857962644001192
      new int[]{20, 6930, 6376, 9680}  //
  };
  private static int[][] dataSet2 = new int[][]{
      new int[]{10, 20, 30, 1000},
      new int[]{5, 10, 15, 1000},
      new int[]{200, 300, 400, 100}
  };


  public static class TestDEASolverScores extends TestThreadFactory {

    private DEASolver.AnalysisType type;
    // MAde null to find where this test is activated from

    public TestDEASolverScores(DEASolver.AnalysisType type) {
      this.type = type;
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {

          DEATestApp app1 = new DEATestApp(dataSet1, type);
          secureComputationEngine
              .runApplication(app1, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          for (int i = 0; i < app1.solverResult.length; i++) {
            Assert.assertEquals(app1.plainResult[i], postProcess(app1.solverResult[i], type,
                app1.modulus), 0.0000001);
          }
          DEATestApp app2 = new DEATestApp(dataSet2, type);
          secureComputationEngine
              .runApplication(app2, SecureComputationEngineImpl.createResourcePool(conf.sceConf,
                  conf.sceConf.getSuite()));
          for (int i = 0; i < app2.solverResult.length; i++) {
            Assert.assertEquals(app2.plainResult[i], postProcess(app2.solverResult[i], type,
                app1.modulus), 0.0000001);
          }
        }
      };
    }
  }

  /**
   * A test application class.
   *
   * <p>
   * Output is conveniently extracted from public fields.
   * </p>
   */
  private static class DEATestApp implements Application {

    private static final long serialVersionUID = 1L;
    double[] plainResult;
    OInt[] solverResult;
    private DEASolver.AnalysisType type;
    private int[][] dataSet;
    private BigInteger modulus;

    DEATestApp(int[][] dataSet, DEASolver.AnalysisType type) {
      this.type = type;
      this.dataSet = dataSet;
    }

    @Override
    public ProtocolProducer prepareApplication(FactoryProducer producer) {
      plainResult = new double[dataSet.length];
      solverResult = new OInt[dataSet.length];
      BasicNumericFactory bnFactory = producer.getBasicNumericFactory();
      NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
      modulus = bnFactory.getModulus();

      BigInteger[][] rawBasisInputs = new BigInteger[dataSet.length][dataSet1[0].length - 1];
      BigInteger[][] rawBasisOutputs = new BigInteger[dataSet.length][1];
      for (int i = 0; i < dataSet.length; i++) {
        for (int j = 0; j < dataSet[0].length - 1; j++) {
          rawBasisInputs[i][j] = BigInteger.valueOf(dataSet[i][j]);
        }
        rawBasisOutputs[i][0] = BigInteger.valueOf(dataSet[i][dataSet[i].length - 1]);
      }

      SInt[][] basisInputs = ioBuilder.inputMatrix(rawBasisInputs, 1);
      SInt[][] basisOutputs = ioBuilder.inputMatrix(rawBasisOutputs, 1);
      SInt[][] targetInputs = ioBuilder.inputMatrix(rawBasisInputs, 2);
      SInt[][] targetOutputs = ioBuilder.inputMatrix(rawBasisOutputs, 2);

      DEASolver solver = new DEASolver(type, AlgebraUtil.arrayToList(targetInputs),
          AlgebraUtil.arrayToList(targetOutputs), AlgebraUtil.arrayToList(basisInputs),
          AlgebraUtil.arrayToList(basisOutputs));

      ioBuilder.addProtocolProducer(solver.prepareApplication(producer));
      solverResult = ioBuilder.outputArray(solver.getResult());

      // Solve the problem using a plaintext solver
      PlaintextDEASolver plainSolver = new PlaintextDEASolver();
      plainSolver.addBasis(rawBasisInputs, rawBasisOutputs);

      double[] plain = plainSolver.solve(rawBasisInputs, rawBasisOutputs, type);
      System.arraycopy(plain, 0, plainResult, 0, plain.length);
      return ioBuilder.getProtocol();
    }
  }

  /**
   * Reduces a field-element to a double using Gauss reduction.
   */
  private static double postProcess(OInt input, AnalysisType type, BigInteger modulus) {
    BigInteger[] gauss = gauss(input.getValue(), modulus);
    double res = (gauss[0].doubleValue() / gauss[1].doubleValue());
    if (type == AnalysisType.OUTPUT_EFFICIENCY) {
      res -= BENCHMARKING_BIG_M;
    } else {
      res *= -1;
    }
    return res;
  }

  /**
   * Converts a number of the form <i>t = r*s<sup>-1</sup> mod N</i> to the rational number
   * <i>r/s</i> represented as a reduced fraction.
   * <p>
   * This is useful outputting non-integer rational numbers from MPC, when outputting a non-reduced
   * fraction may leak too much information. The technique used is adapted from the paper
   * "CryptoComputing With Rationals" of Fouque et al. Financial Cryptography 2002. This methods
   * restricts us to integers <i>t = r*s<sup>-1</sup> mod N</i> so that <i>2r*s < N</i>. See
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
