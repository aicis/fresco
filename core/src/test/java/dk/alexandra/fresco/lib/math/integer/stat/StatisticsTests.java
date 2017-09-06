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
 */
package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;


/**
 * Generic test cases for basic finite field operations.
 *
 * Can be reused by a test case for any protocol suite that implements the basic field protocol
 * factory.
 *
 * TODO: Generic tests should not reside in the runtime package. Rather in mpc.lib or something.
 */
public class StatisticsTests {

  public static class TestStatistics<ResourcePoolT extends ResourcePool> extends TestThreadFactory {

    @Override
    public TestThread next() {
      //TODO Should be split into different tests for mean, variance, covariance, covariancematrix
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<Integer> data1 = Arrays.asList(543, 520, 532, 497, 450, 432);
        private final List<Integer> data2 = Arrays.asList(432, 620, 232, 337, 250, 433);
        private final List<Integer> data3 = Arrays.asList(80, 90, 123, 432, 145, 606);
        private final List<Integer> dataMean = Arrays.asList(496, 384);

        private Computation<BigInteger> outputMean1;
        private Computation<BigInteger> outputMean2;
        private Computation<BigInteger> outputVariance;
        private Computation<BigInteger> outputCovariance;
        private List<List<Computation<BigInteger>>> outputCovarianceMatix;

        @Override
        public void test() throws Exception {
          Application<Void, ProtocolBuilderNumeric> app =
              builder -> {
                NumericBuilder NumericBuilder = builder.numeric();
                List<Computation<SInt>> input1 = data1.stream()
                    .map(BigInteger::valueOf)
                    .map(NumericBuilder::known)
                    .collect(Collectors.toList());
                List<Computation<SInt>> input2 = data2.stream()
                    .map(BigInteger::valueOf)
                    .map(NumericBuilder::known)
                    .collect(Collectors.toList());
                List<Computation<SInt>> input3 = data3.stream()
                    .map(BigInteger::valueOf)
                    .map(NumericBuilder::known)
                    .collect(Collectors.toList());

                List<Computation<SInt>> means = dataMean.stream()
                    .map(BigInteger::valueOf)
                    .map(NumericBuilder::known)
                    .collect(Collectors.toList());

                Computation<SInt> mean1 = builder
                    .seq(new Mean(input1));
                Computation<SInt> mean2 = builder
                    .seq(new Mean(input2));
                Computation<SInt> variance = builder
                    .seq(new Variance(input1, mean1));
                Computation<SInt> covariance = builder
                    .seq(new Covariance(input1, input2, mean1, mean2));
                Computation<List<List<Computation<SInt>>>> covarianceMatrix = builder
                    .seq(
                        new CovarianceMatrix(Arrays.asList(input1, input2, input3), means));

                return builder.par((par) -> {
                  NumericBuilder open = par.numeric();
                  outputMean1 = open.open(mean1);
                  outputMean2 = open.open(mean2);
                  outputVariance = open.open(variance);
                  outputCovariance = open.open(covariance);
                  List<List<Computation<SInt>>> covarianceMatrixOut = covarianceMatrix.out();
                  List<List<Computation<BigInteger>>> openCovarianceMatrix = new ArrayList<>(
                      covarianceMatrixOut.size());
                  for (List<Computation<SInt>> computations : covarianceMatrixOut) {
                    List<Computation<BigInteger>> computationList = new ArrayList<>(
                        computations.size());
                    openCovarianceMatrix.add(computationList);
                    for (Computation<SInt> computation : computations) {
                      computationList.add(open.open(computation));
                    }
                  }
                  outputCovarianceMatix = openCovarianceMatrix;
                  return () -> null;
                });
              };
          secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
          BigInteger mean1 = outputMean1.out();
          BigInteger mean2 = outputMean2.out();
          BigInteger variance = outputVariance.out();
          BigInteger covariance = outputCovariance.out();

          double sum = 0.0;
          for (int entry : data1) {
            sum += entry;
          }
          double mean1Exact = sum / data1.size();

          sum = 0.0;
          for (int entry : data2) {
            sum += entry;
          }
          double mean2Exact = sum / data2.size();

          double ssd = 0.0;
          for (int entry : data1) {
            ssd += (entry - mean1Exact) * (entry - mean1Exact);
          }
          double varianceExact = ssd / (data1.size() - 1);

          double covarianceExact = 0.0;
          for (int i = 0; i < data1.size(); i++) {
            covarianceExact += (data1.get(i) - mean1Exact) * (data2.get(i) - mean2Exact);
          }
          covarianceExact /= (data1.size() - 1);

          double tolerance = 1.0;
          Assert.assertTrue(isInInterval(mean1, mean1Exact, tolerance));
          Assert.assertTrue(isInInterval(mean2, mean2Exact, tolerance));
          Assert.assertTrue(isInInterval(variance, varianceExact, tolerance));
          System.out.println(covariance + " " + covarianceExact + " - " + tolerance);
          Assert.assertTrue(isInInterval(covariance, covarianceExact, tolerance));
          Assert.assertTrue(
              isInInterval(outputCovarianceMatix.get(0).get(0).out(), varianceExact,
                  tolerance));
          Assert
              .assertTrue(isInInterval(outputCovarianceMatix.get(1).get(0).out(),
                  covarianceExact, tolerance));

        }
      };
    }

    private static boolean isInInterval(BigInteger value, double center, double tolerance) {
      return value.intValue() >= center - tolerance && value.intValue() <= center + tolerance;
    }
  }


  public static class TestStatisticsNoMean<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory {

    @Override
    public TestThread next() {

      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private final List<Integer> data1 = Arrays.asList(543, 520, 532, 497, 450, 432);
        private final List<Integer> data2 = Arrays.asList(432, 620, 232, 337, 250, 433);
        private final List<Integer> data3 = Arrays.asList(80, 90, 123, 432, 145, 606);

        private Computation<BigInteger> outputCovariance;
        private List<List<Computation<BigInteger>>> outputCovarianceMatix;


        @Override
        public void test() throws Exception {
          Application<Void, ProtocolBuilderNumeric> app =
              builder -> {
                NumericBuilder NumericBuilder = builder.numeric();
                List<Computation<SInt>> input1 = data1.stream()
                    .map(BigInteger::valueOf)
                    .map(NumericBuilder::known)
                    .collect(Collectors.toList());
                List<Computation<SInt>> input2 = data2.stream()
                    .map(BigInteger::valueOf)
                    .map(NumericBuilder::known)
                    .collect(Collectors.toList());
                List<Computation<SInt>> input3 = data3.stream()
                    .map(BigInteger::valueOf)
                    .map(NumericBuilder::known)
                    .collect(Collectors.toList());

                Computation<SInt> covariance = builder.seq(new Covariance(input1, input2));
                Computation<List<List<Computation<SInt>>>> covarianceMatrix = builder
                    .seq(new CovarianceMatrix(Arrays.asList(input1, input2, input3)));

                return builder.par((par) -> {
                  NumericBuilder open = par.numeric();
                  outputCovariance = open.open(covariance);
                  List<List<Computation<SInt>>> covarianceMatrixOut = covarianceMatrix.out();
                  List<List<Computation<BigInteger>>> openCovarianceMatrix = new ArrayList<>(
                      covarianceMatrixOut.size());
                  for (List<Computation<SInt>> computations : covarianceMatrixOut) {
                    List<Computation<BigInteger>> computationList = new ArrayList<>(
                        computations.size());
                    openCovarianceMatrix.add(computationList);
                    for (Computation<SInt> computation : computations) {
                      computationList.add(open.open(computation));
                    }
                  }
                  outputCovarianceMatix = openCovarianceMatrix;
                  return () -> null;
                });
              };

          secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
          BigInteger covariance = outputCovariance.out();
          double sum = 0.0;
          for (int entry : data1) {
            sum += entry;
          }
          double mean1Exact = sum / data1.size();

          sum = 0.0;
          for (int entry : data2) {
            sum += entry;
          }
          double mean2Exact = sum / data2.size();

          double ssd = 0.0;
          for (int entry : data1) {
            ssd += (entry - mean1Exact) * (entry - mean1Exact);
          }
          double varianceExact = ssd / (data1.size() - 1);

          double covarianceExact = 0.0;
          for (int i = 0; i < data1.size(); i++) {
            covarianceExact += (data1.get(i) - mean1Exact) * (data2.get(i) - mean2Exact);
          }
          covarianceExact /= (data1.size() - 1);

          double tolerance = 1.0;
          Assert.assertTrue(isInInterval(covariance, covarianceExact, tolerance));
          Assert.assertTrue(
              isInInterval(outputCovarianceMatix.get(0).get(0).out(), varianceExact,
                  tolerance));
          Assert
              .assertTrue(isInInterval(outputCovarianceMatix.get(1).get(0).out(),
                  covarianceExact, tolerance));
        }
      };
    }

    private static boolean isInInterval(BigInteger value, double center, double tolerance) {
      return value.intValue() >= center - tolerance && value.intValue() <= center + tolerance;
    }
  }

}
