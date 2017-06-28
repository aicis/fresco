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
package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementFactory;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPFactory;
import dk.alexandra.fresco.lib.lp.LPFactoryImpl;
import dk.alexandra.fresco.lib.lp.LPPrefix;
import dk.alexandra.fresco.lib.lp.LPSolverProtocol4;
import dk.alexandra.fresco.lib.lp.LPSolverProtocol4.LPOutput;
import dk.alexandra.fresco.lib.lp.Matrix;
import dk.alexandra.fresco.lib.lp.Matrix4;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.suite.spdz.utils.LPInputReader;
import dk.alexandra.fresco.suite.spdz.utils.PlainLPInputReader;
import dk.alexandra.fresco.suite.spdz.utils.PlainSpdzLPPrefix;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;

class LPSolverTests {

  public static class TestLPSolver<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT> {

    @Override
    public TestThread next(TestThreadConfiguration<ResourcePoolT> conf) {
      return new TestThread<ResourcePoolT>() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory bnFactory = (BasicNumericFactory) producer;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) producer;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) producer;
              RandomFieldElementFactory randFactory = (RandomFieldElementFactory) producer;
              LPFactory lpFactory = new LPFactoryImpl(80, bnFactory,
                  expFromOIntFactory, expFactory, randFactory,
                  (BuilderFactoryNumeric) factoryProducer);
              File pattern = new File("src/test/resources/lp/pattern7.csv");
              File program = new File("src/test/resources/lp/program7.csv");
              LPInputReader inputreader;
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
              return ProtocolBuilder
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    LPPrefix prefix;
                    try {
                      prefix = new PlainSpdzLPPrefix(inputreader,
                          bnFactory);
                    } catch (IOException e) {
                      e.printStackTrace();
                      throw new MPCException("IOException: "
                          + e.getMessage(), e);
                    }
                    builder.append(prefix.getPrefix());
                    Matrix<SInt> updateMatrix = prefix.getUpdateMatrix();
                    Computation<LPOutput> lpOutput = builder.createSequentialSub(
                        new LPSolverProtocol4(
                            prefix.getTableau(),
                            new Matrix4<>(updateMatrix.getWidth(), updateMatrix.getHeight(),
                                i -> new ArrayList<>(Arrays.asList(updateMatrix.getIthRow(i)))),
                            prefix.getPivot(),
                            bnFactory));

                    builder.createIteration((seq) -> {
                      SInt sout = bnFactory.getSInt();
                      LPOutput lpOut = lpOutput.out();
                      Matrix4<Computation<SInt>> finalUpdateMatrix = lpOut.updateMatrix;
                      SInt[][] matrix = new SInt[finalUpdateMatrix.getHeight()][finalUpdateMatrix
                          .getWidth()];
                      for (int i = 0; i < finalUpdateMatrix.getHeight(); i++) {
                        matrix[i] = finalUpdateMatrix.getRow(i).stream().map(Computation::out)
                            .toArray(SInt[]::new);
                      }
                      ProtocolProducer outputter = lpFactory
                          .getOptimalValueProtocol(
                              new Matrix<>(matrix),
                              lpOut.tableau.getB(),
                              lpOut.pivot,
                              sout);
                      Computation<BigInteger> openProtocol = bnFactory
                          .getOpenProtocol(sout);
                      seq.append(new SequentialProtocolProducer(
                          outputter,
                          openProtocol
                      ));
                      this.outputs.add(openProtocol);
                    });
                  }).build();
            }
          };
          long startTime = System.nanoTime();
          ResourcePoolT resourcePool = SecureComputationEngineImpl.createResourcePool(
              conf.sceConf, conf.sceConf.getSuite());
          secureComputationEngine.runApplication(app, resourcePool);
          long endTime = System.nanoTime();
          System.out.println("============ Seq Time: "
              + ((endTime - startTime) / 1000000));
          Assert.assertTrue(BigInteger.valueOf(161).equals(app.getOutputs()[0]));
        }
      };
    }
  }
}
