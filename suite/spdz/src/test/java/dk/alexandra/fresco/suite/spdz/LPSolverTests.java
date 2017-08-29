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

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.lp.LPSolver;
import dk.alexandra.fresco.lib.lp.LPSolver.LPOutput;
import dk.alexandra.fresco.lib.lp.LPSolver.PivotRule;
import dk.alexandra.fresco.lib.lp.OptimalValue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.Assert;

class LPSolverTests {

  public static class TestLPSolver<ResourcePoolT extends ResourcePool> extends
      TestThreadFactory {

    private final PivotRule pivotRule;

    public TestLPSolver(PivotRule pivotRule) {
      this.pivotRule = pivotRule;
    }

    @Override
    public TestThread next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            File pattern = new File("src/test/resources/lp/pattern7.csv");
            File program = new File("src/test/resources/lp/program7.csv");
            PlainLPInputReader inputreader;
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
            return builder.par(par -> {
              PlainSpdzLPPrefix prefix;
              try {
                prefix = new PlainSpdzLPPrefix(inputreader, par);
              } catch (IOException e) {
                e.printStackTrace();
                throw new MPCException("IOException: "
                    + e.getMessage(), e);
              }
              return () -> prefix;
            }).seq((seq, prefix) -> {
              Computation<LPOutput> lpOutput = seq.seq(
                  new LPSolver(
                      pivotRule,
                      prefix.getTableau(),
                      prefix.getUpdateMatrix(),
                      prefix.getPivot(),
                      prefix.getBasis()));

              Computation<SInt> optimalValue = seq.seq((inner) -> {
                    LPOutput out = lpOutput.out();
                    return new OptimalValue(out.updateMatrix, out.tableau, out.pivot)
                        .buildComputation(inner);
                  }
              );
              Computation<BigInteger> open = seq.numeric().open(optimalValue);
              return open;
            });
          };
          long startTime = System.nanoTime();
          ResourcePoolT resourcePool = ResourcePoolCreator.createResourcePool(
              conf.sceConf);
          BigInteger result = secureComputationEngine.runApplication(app, resourcePool);
          long endTime = System.nanoTime();
          System.out.println("============ Seq Time: "
              + ((endTime - startTime) / 1000000));
          Assert.assertTrue(BigInteger.valueOf(161).equals(result));
        }
      };
    }
  }
}
