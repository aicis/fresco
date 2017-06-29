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
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.lp.LPSolverProtocol4;
import dk.alexandra.fresco.lib.lp.LPSolverProtocol4.LPOutput;
import dk.alexandra.fresco.lib.lp.Matrix;
import dk.alexandra.fresco.lib.lp.Matrix4;
import dk.alexandra.fresco.lib.lp.OptimalValue4;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;

class LPSolverTests {

  public static class TestLPSolver<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread next(TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              BasicNumericFactory bnFactory = (BasicNumericFactory) producer;
              File pattern = new File("src/test/resources/lp/pattern7.csv");
              File program = new File("src/test/resources/lp/program7.csv");
              PlainLPInputReader4 inputreader;
              try {
                inputreader = PlainLPInputReader4
                    .getFileInputReader(program, pattern,
                        conf.getMyId());
              } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new MPCException(
                    "Could not read needed files: "
                        + e.getMessage(), e);
              }
              return ProtocolBuilderNumeric
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer, (builder) -> {
                    Computation<PlainSpdzLPPrefix4> prefixComp = builder.par(par -> {
                      PlainSpdzLPPrefix4 prefix;
                      try {
                        prefix = new PlainSpdzLPPrefix4(inputreader, par);
                      } catch (IOException e) {
                        e.printStackTrace();
                        throw new MPCException("IOException: "
                            + e.getMessage(), e);
                      }
                      return () -> prefix;
                    });
                    builder.createSequentialSub((seq) -> {
                      PlainSpdzLPPrefix4 prefix = prefixComp.out();
                      Computation<LPOutput> lpOutput = seq.createSequentialSub(
                          new LPSolverProtocol4(
                              prefix.getTableau(),
                              prefix.getUpdateMatrix(),
                              prefix.getPivot()
                          ));

                      Computation<SInt> optimalValue = seq.createSequentialSub((inner) -> {
                            LPOutput out = lpOutput.out();
                            return new OptimalValue4(out.updateMatrix, out.tableau, out.pivot)
                                .build(inner);
                          }
                      );
                      Computation<BigInteger> open = seq.numeric().open(optimalValue);
                      this.outputs.add(open);
                      return () -> null;
                    });
                  }).build();
            }

            Matrix4<Computation<SInt>> toMatrix4(Matrix<SInt> updateMatrix) {
              return new Matrix4<>(updateMatrix.getHeight(), updateMatrix.getWidth(),
                  i -> new ArrayList<>(Arrays.asList(updateMatrix.getIthRow(i))));
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
