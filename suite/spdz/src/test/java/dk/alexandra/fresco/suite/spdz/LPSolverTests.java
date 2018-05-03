package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.lp.LPSolver;
import dk.alexandra.fresco.lib.lp.LPSolver.PivotRule;
import dk.alexandra.fresco.lib.lp.OptimalValue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import org.junit.Assert;

class LPSolverTests {

  public static class TestLPSolver<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final PivotRule pivotRule;

    public TestLPSolver(PivotRule pivotRule) {
      this.pivotRule = pivotRule;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() {
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            File pattern = new File("src/test/resources/lp/pattern7.csv");
            File program = new File("src/test/resources/lp/program7.csv");
            PlainLPInputReader inputreader;
            try {
              inputreader = PlainLPInputReader.getFileInputReader(program, pattern, conf.getMyId());
            } catch (FileNotFoundException e) {
              e.printStackTrace();
              throw new IllegalArgumentException("Could not read needed files ", e);
            }
            return builder.par(par -> {
              PlainSpdzLPPrefix prefix;
              try {
                prefix = new PlainSpdzLPPrefix(inputreader, par);
              } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("IOException: " + e.getMessage(), e);
              }
              return () -> prefix;
            }).seq((seq, prefix) -> seq
                .seq(new LPSolver(pivotRule, prefix.getTableau(),
                    prefix.getUpdateMatrix(), prefix.getPivot(), prefix.getBasis()))
                .seq((inner, out) ->
                    new OptimalValue(out.updateMatrix, out.tableau, out.pivot)
                        .buildComputation(inner))
                .seq((inner, values) -> inner.numeric().open(values.optimal)));
          };
          long startTime = System.nanoTime();
          BigInteger result = runApplication(app);
          long endTime = System.nanoTime();
          System.out.println("============ Seq Time: " + ((endTime - startTime) / 1000000));
          Assert.assertEquals(BigInteger.valueOf(161), result);
        }
      };
    }
  }
}
