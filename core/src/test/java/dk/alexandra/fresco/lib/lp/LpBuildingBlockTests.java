package dk.alexandra.fresco.lib.lp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class LpBuildingBlockTests {

  private abstract static class LpTester<OutputT>
      implements Application<OutputT, ProtocolBuilderNumeric> {

    Random rand = new Random(42);
    Matrix<BigInteger> updateMatrix;
    Matrix<BigInteger> constraints;
    ArrayList<BigInteger> b;
    protected ArrayList<BigInteger> f;
    LPTableau secretTableau;
    Matrix<DRes<SInt>> secretUpdateMatrix;

    void inputTableau(ProtocolBuilderNumeric builder) {
      builder.par(par -> {
        Numeric numeric = par.numeric();
        secretTableau = new LPTableau(
            new Matrix<>(constraints.getHeight(), constraints.getWidth(),
                (i) -> toArrayList(numeric, constraints.getRow(i))),
            toArrayList(numeric, b), toArrayList(numeric, f), numeric.known(BigInteger.ZERO));
        secretUpdateMatrix = new Matrix<>(updateMatrix.getHeight(), updateMatrix.getWidth(),
            (i) -> toArrayList(numeric, updateMatrix.getRow(i)));
        return null;
      });
    }

    private ArrayList<DRes<SInt>> toArrayList(Numeric numeric, ArrayList<BigInteger> row) {
      return new ArrayList<>(row.stream().map(numeric::known).collect(Collectors.toList()));
    }

    Matrix<BigInteger> randomMatrix(int n, int m) {
      return new Matrix<>(n, m, (i) -> randomList(m));
    }

    ArrayList<BigInteger> randomList(int m) {
      ArrayList<BigInteger> result = new ArrayList<>(m);
      while (result.size() < m) {
        result.add(new BigInteger(32, rand));
      }
      return result;
    }
  }

  private abstract static class EnteringVariableTester extends LpTester<List<BigInteger>> {

    private int expectedIndex;

    int getExpextedIndex() {
      return expectedIndex;
    }

    DRes<List<BigInteger>> setup(ProtocolBuilderNumeric builder) {
      updateMatrix = new Matrix<>(5, 5, i -> {
        ArrayList<BigInteger> row =
            new ArrayList<>(Arrays.asList(((i == 0) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 1) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 2) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 3) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 4) ? BigInteger.ONE : BigInteger.ZERO)));
        return row;
      }); // The identity matrix
      constraints = randomMatrix(4, 5); // this is irrelevant
      b = randomList(4); // this is irrelevant
      f = new ArrayList<>(5);
      f.add(BigInteger.valueOf(0));
      f.add(BigInteger.valueOf(2));
      f.add(BigInteger.valueOf(3));
      f.add(BigInteger.valueOf(-2));
      f.add(BigInteger.valueOf(-5));// index 4 is the correct choice (minimum entry)
      inputTableau(builder);
      expectedIndex = 4;

      return builder.seq(
          (seq) -> new EnteringVariable(secretTableau, secretUpdateMatrix).buildComputation(seq))
          .seq((seq, enteringOutput) -> {
            List<DRes<SInt>> enteringIndex = enteringOutput.getFirst();
            Numeric numeric = seq.numeric();
            List<DRes<BigInteger>> opened =
                enteringIndex.stream().map(numeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          });
    }

  }

  private abstract static class BlandEnteringVariableTester extends LpTester<List<BigInteger>> {

    private int expectedIndex;

    @SuppressWarnings("unused")
    int getExpextedIndex() {
      return expectedIndex;
    }

    DRes<List<BigInteger>> setup(ProtocolBuilderNumeric builder) {
      updateMatrix = new Matrix<>(5, 5, i -> {
        ArrayList<BigInteger> row =
            new ArrayList<>(Arrays.asList(((i == 0) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 1) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 2) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 3) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 4) ? BigInteger.ONE : BigInteger.ZERO)));
        return row;
      }); // The identity matrix
      constraints = randomMatrix(4, 5); // this is irrelevant
      b = randomList(4); // this is irrelevant
      f = new ArrayList<>(5);
      f.add(BigInteger.valueOf(0));
      f.add(BigInteger.valueOf(2));
      f.add(BigInteger.valueOf(3));
      f.add(BigInteger.valueOf(-2)); // index 3 is the correct choice (first less than zero)
      f.add(BigInteger.valueOf(-5));
      inputTableau(builder);
      expectedIndex = 3;
      return builder.seq((seq) -> new BlandEnteringVariable(secretTableau, secretUpdateMatrix)
          .buildComputation(seq)).seq((seq, enteringOutput) -> {
            List<DRes<SInt>> enteringIndex = enteringOutput.getFirst();
            Numeric numeric = seq.numeric();
            List<DRes<BigInteger>> opened =
                enteringIndex.stream().map(numeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          });
    }
  }

  private abstract static class LpTabluauTester extends LpTester<Object> {

    void setup(ProtocolBuilderNumeric builder, PrintStream ps) {
      updateMatrix = new Matrix<>(5, 5, i -> {
        ArrayList<BigInteger> row =
            new ArrayList<>(Arrays.asList(((i == 0) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 1) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 2) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 3) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 4) ? BigInteger.ONE : BigInteger.ZERO)));
        return row;
      }); // The identity matrix
      constraints = new Matrix<>(4, 3, i -> {
        ArrayList<BigInteger> row = new ArrayList<>(
            Arrays.asList(BigInteger.valueOf(i), BigInteger.valueOf(i), BigInteger.valueOf(i)));
        return row;
      }); // each entry has the value of the row number
      b = new ArrayList<>(Arrays.asList(BigInteger.valueOf(10), BigInteger.valueOf(10),
          BigInteger.valueOf(10), BigInteger.valueOf(10)));
      f = new ArrayList<>(
          Arrays.asList(BigInteger.valueOf(0), BigInteger.valueOf(2), BigInteger.valueOf(3)));
      inputTableau(builder);
      builder.seq(seq -> {
        secretTableau.debugInfo(seq, ps);
        return null;
      });
      return;
    }
  }

  private abstract static class LpSolverTester extends LpTester<BigInteger> {

    BigInteger expectedOptimal;

    public BigInteger getExpectedOptimal() {
      return expectedOptimal;
    }

    DRes<BigInteger> setup(ProtocolBuilderNumeric builder, LPSolver.PivotRule rule) {
      /*
       * 
       * Sets up the following linear program 
       * maximize a + b + c 
       * a <= 1 
       * b <= 2 
       * c <= 3 (all variables > 0 is implied)
       * 
       */
      expectedOptimal = BigInteger.valueOf(6);
      updateMatrix = new Matrix<>(4, 4, i -> {
        ArrayList<BigInteger> row =
            new ArrayList<>(Arrays.asList(((i == 0) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 1) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 2) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 3) ? BigInteger.ONE : BigInteger.ZERO)));
        return row;
      }); // The identity matrix
      constraints = new Matrix<>(3, 6, i -> {
        ArrayList<BigInteger> row =
            new ArrayList<>(Arrays.asList(((i == 0) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 1) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 2) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 0) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 1) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 2) ? BigInteger.ONE : BigInteger.ZERO)));
        return row;
      }); // A 3x3 identity matrix concatenated with a 3x3 identity matrix
      b = new ArrayList<>(Arrays.asList(
          BigInteger.valueOf(1), 
          BigInteger.valueOf(2), 
          BigInteger.valueOf(3)));
      f = new ArrayList<>(Arrays.asList(
          BigInteger.valueOf(-1), 
          BigInteger.valueOf(-1),
          BigInteger.valueOf(-1), 
          BigInteger.ZERO, 
          BigInteger.ZERO, 
          BigInteger.ZERO));
      inputTableau(builder);
      return builder.seq(seq -> {
        DRes<SInt> pivot = seq.numeric().known(BigInteger.ONE);
        ArrayList<DRes<SInt>> initialBasis =
            new ArrayList<>(Arrays.asList(
                seq.numeric().known(BigInteger.valueOf(4)),
                seq.numeric().known(BigInteger.valueOf(5)),
                seq.numeric().known(BigInteger.valueOf(6))));
        LPSolver solver = new LPSolver(rule, secretTableau, secretUpdateMatrix,
            pivot, initialBasis);
        return solver.buildComputation(seq);
      }).seq((seq2, lpOutput) -> {
        OptimalValue ov = new OptimalValue(lpOutput.updateMatrix, lpOutput.tableau, lpOutput.pivot);
        return seq2.numeric().open(ov.buildComputation(seq2));
      });
    }
  }

  private abstract static class LpSolverDebugTester extends LpTester<BigInteger> {

    DRes<BigInteger> setup(ProtocolBuilderNumeric builder, LPSolver.PivotRule rule) {
      /*
       * 
       * Sets up the following linear program 
       * maximize a + b + c 
       * a <= 1 
       * b <= 2 
       * c <= 3 (all variables > 0 is implied)
       * 
       */
      updateMatrix = new Matrix<>(4, 4, i -> {
        ArrayList<BigInteger> row =
            new ArrayList<>(Arrays.asList(
                ((i == 0) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 1) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 2) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 3) ? BigInteger.ONE : BigInteger.ZERO)));
        return row;
      }); // The identity matrix
      constraints = new Matrix<>(3, 6, i -> {
        ArrayList<BigInteger> row =
            new ArrayList<>(Arrays.asList(
                ((i == 0) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 1) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 2) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 0) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 1) ? BigInteger.ONE : BigInteger.ZERO),
                ((i == 2) ? BigInteger.ONE : BigInteger.ZERO)));
        return row;
      }); // A 3x3 identity matrix concatenated with a 3x3 identity matrix
      b = new ArrayList<>(Arrays.asList(
          BigInteger.valueOf(1), 
          BigInteger.valueOf(2), 
          BigInteger.valueOf(3)));
      f = new ArrayList<>(Arrays.asList(
          BigInteger.valueOf(-1), 
          BigInteger.valueOf(-1),
          BigInteger.valueOf(-1), 
          BigInteger.ZERO, 
          BigInteger.ZERO, 
          BigInteger.ZERO));
      inputTableau(builder);
      return builder.seq(seq -> {
        DRes<SInt> pivot = seq.numeric().known(BigInteger.ONE);
        ArrayList<DRes<SInt>> initialBasis =
            new ArrayList<>(Arrays.asList(
                seq.numeric().known(BigInteger.valueOf(4)),
                seq.numeric().known(BigInteger.valueOf(5)),
                seq.numeric().known(BigInteger.valueOf(6))));
        LPSolver solver = new LPSolver(LPSolver.PivotRule.DANZIG, secretTableau, secretUpdateMatrix,
            pivot, initialBasis) {
          
          @Override
          protected boolean isDebug() {
            return true;
          }
        };

        return solver.buildComputation(seq);
      }).seq((seq2, lpOutput) -> {
        OptimalValue ov = new OptimalValue(lpOutput.updateMatrix, lpOutput.tableau, lpOutput.pivot);
        return seq2.numeric().open(ov.buildComputation(seq2));
      });
    }
  }

  public static class TestLpTableuDebug<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    PrintStream stream = new PrintStream(bytes);

    public TestLpTableuDebug() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          LpTabluauTester app = new LpTabluauTester() {

            @Override
            public DRes<Object> buildComputation(ProtocolBuilderNumeric builder) {
              setup(builder, stream);
              return () -> null;
            }
          };
          runApplication(app);
          String debugOutput = bytes.toString("UTF-8");
          assertDebugInfoContains(debugOutput, "C", "0, 0, 0, \n" 
              + "1, 1, 1, \n" 
              + "2, 2, 2, \n" 
              + "3, 3, 3,");
          assertDebugInfoContains(debugOutput, "B", "10, 10, 10, 10,");
          assertDebugInfoContains(debugOutput, "F", "0, 2, 3,");
          assertDebugInfoContains(debugOutput, "z", "0");
        }
      };
    }
  }

  public static class TestLpSolver<ResourcePoolT extends ResourcePool> 
  extends TestThreadFactory <ResourcePoolT, ProtocolBuilderNumeric> {

    private LPSolver.PivotRule pivotRule; 
    
    public TestLpSolver(LPSolver.PivotRule pivotRule) {
      this.pivotRule = pivotRule;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          LpSolverTester app = new LpSolverTester() {

            @Override
            public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric builder) {
              return setup(builder, pivotRule);
            }
          };
          BigInteger out = runApplication(app);
          assertEquals(app.getExpectedOptimal(), out);
        }
      };
    }
  }
  
  public static class TestLpSolverDebug<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    public TestLpSolverDebug() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          LpSolverDebugTester app = new LpSolverDebugTester() {

            @Override
            public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric builder) {
              return setup(builder, LPSolver.PivotRule.DANZIG);
            }
          };
          if (this.conf.getMyId() == 1) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream stdout = System.out;
            System.setOut(new PrintStream(out));
            runApplication(app);
            String debugOutput = out.toString();
            System.setOut(stdout);
            System.out.println(debugOutput);
            debugOutput.replaceAll("\r", "");
            assertDebugInfoContains(debugOutput, "C", "1, 0, 0, 1, 0, 0, \n" 
                + "0, 1, 0, 0, 1, 0, \n" 
                + "0, 0, 1, 0, 0, 1,");
            assertDebugInfoContains(debugOutput, "B", "1, 2, 3, ");
            assertDebugInfoContains(debugOutput, "F", "-1, -1, -1, 0, 0, 0, ");
            assertDebugInfoContains(debugOutput, "z", "0");
            assertDebugInfoContains(debugOutput, "Basis [1]", "4, 5, 6,");
            assertDebugInfoContains(debugOutput, "Basis [4]", "1, 2, 3,");
            assertDebugInfoContains(debugOutput, "Update Matrix [1]", "1, 0, 0, 0, \n" 
                + "0, 1, 0, 0, \n"
                + "0, 0, 1, 0, \n" 
                + "0, 0, 0, 1, ");
          } else {
            runApplication(app);
          }
        }
      };
    }
  }

  public static class TestEnteringVariable<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    public TestEnteringVariable() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          EnteringVariableTester app = new EnteringVariableTester() {


            @Override
            public DRes<List<BigInteger>> buildComputation(ProtocolBuilderNumeric builder) {
              return setup(builder);
            }
          };
          List<BigInteger> outputs = runApplication(app);
          int actualIndex = 0;
          int sum = 0;
          BigInteger zero = BigInteger.ZERO;
          BigInteger one = BigInteger.ONE;
          for (BigInteger b : outputs) {
            if (b.compareTo(zero) == 0) {
              actualIndex = (sum < 1) ? actualIndex + 1 : actualIndex;
            } else {
              assertEquals(one, b);
              sum++;
            }
          }
          assertEquals(1, sum);
          assertEquals(app.getExpextedIndex(), actualIndex);
        }
      };
    }
  }

  public static class TestBlandEnteringVariable<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    public TestBlandEnteringVariable() {}

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          BlandEnteringVariableTester app = new BlandEnteringVariableTester() {


            @Override
            public DRes<List<BigInteger>> buildComputation(ProtocolBuilderNumeric builder) {
              return setup(builder);
            }
          };
          List<BigInteger> outputs = runApplication(app);
          int actualIndex = 0;
          int sum = 0;
          BigInteger zero = BigInteger.ZERO;
          BigInteger one = BigInteger.ONE;
          for (BigInteger b : outputs) {
            if (b.compareTo(zero) == 0) {
              actualIndex = (sum < 1) ? actualIndex + 1 : actualIndex;
            } else {
              assertEquals(one, b);
              sum++;
            }
          }
          assertEquals(1, sum);
          assertEquals(app.getExpextedIndex(), actualIndex);
        }
      };
    }    
  }
  
  /**
   * Convenience method to assert that debug output should contain a given key/value pair.
   *  
   * @param output the debug output
   * @param key the key
   * @param value the value
   */
  private static void assertDebugInfoContains(String output, String key, String value) {
    assertTrue(output.contains(key + ": \n" + value));
  }
  
  
}
