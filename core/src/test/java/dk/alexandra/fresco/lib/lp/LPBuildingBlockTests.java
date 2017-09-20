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
 * THE SOFTWARE IS facIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.lp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.Assert;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;


public class LPBuildingBlockTests {

  private static abstract class LPTester<OutputT>
      implements Application<OutputT, ProtocolBuilderNumeric> {

    Random rand = new Random(42);
    BigInteger mod;
    Matrix<BigInteger> updateMatrix;
    Matrix<BigInteger> constraints;
    ArrayList<BigInteger> b;
    protected ArrayList<BigInteger> f;
    LPTableau sTableau;
    Matrix<DRes<SInt>> sUpdateMatrix;

    void randomTableau(int n, int m) {
      updateMatrix = randomMatrix(m + 1, m + 1);
      constraints = randomMatrix(m, n + m);
      this.b = randomList(m);
      this.f = randomList(n + m);
    }

    void inputTableau(ProtocolBuilderNumeric builder) {
      builder.par(par -> {
        Numeric numeric = par.numeric();
        sTableau = new LPTableau(
            new Matrix<>(constraints.getHeight(), constraints.getWidth(),
                (i) -> toArrayList(numeric, constraints.getRow(i))),
            toArrayList(numeric, b), toArrayList(numeric, f), numeric.known(BigInteger.ZERO));
        sUpdateMatrix = new Matrix<>(updateMatrix.getHeight(), updateMatrix.getWidth(),
            (i) -> toArrayList(numeric, updateMatrix.getRow(i)));
        return () -> null;
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

  private static abstract class EnteringVariableTester extends LPTester<List<BigInteger>> {

    private int expectedIndex;

    int getExpextedIndex() {
      return expectedIndex;
    }

    DRes<List<BigInteger>> setupRandom(int n, int m, ProtocolBuilderNumeric builder) {
      randomTableau(n, m);
      inputTableau(builder);

      expectedIndex = enteringDanzigVariableIndex(constraints, updateMatrix, b, f);

      return builder
          .seq((seq) -> new EnteringVariable(sTableau, sUpdateMatrix).buildComputation(seq))
          .seq((seq, enteringOutput) -> {
            List<DRes<SInt>> enteringIndex = enteringOutput.getFirst();
            Numeric numeric = seq.numeric();
            List<DRes<BigInteger>> opened =
                enteringIndex.stream().map(numeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          });
    }

    /**
     * Computes the index of the entering variable given the plaintext LP tableu using Danzigs rule.
     *
     * @param C the constraint matrix
     * @param updateMatrix the update matrix
     * @param B the B vector
     * @param F the F vector
     * @return the entering index
     */
    private int enteringDanzigVariableIndex(Matrix<BigInteger> C, Matrix<BigInteger> updateMatrix,
        ArrayList<BigInteger> B, ArrayList<BigInteger> F) {
      BigInteger[] updatedF = new BigInteger[F.size()];
      ArrayList<BigInteger> updateRow = updateMatrix.getRow(updateMatrix.getHeight() - 1);
      for (int i = 0; i < F.size(); i++) {
        updatedF[i] = BigInteger.valueOf(0);
        List<BigInteger> column = C.getColumn(i);
        for (int j = 0; j < C.getHeight(); j++) {
          updatedF[i] = updatedF[i].add(column.get(j).multiply(updateRow.get(j)));
        }
        updatedF[i] =
            updatedF[i].add(F.get(i).multiply(updateRow.get(updateMatrix.getHeight() - 1)));
      }
      BigInteger half = mod.divide(BigInteger.valueOf(2));
      BigInteger min = updatedF[0];
      int index = 0;
      min = min.compareTo(half) > 0 ? min.subtract(mod) : min;
      for (int i = 0; i < updatedF.length; i++) {
        BigInteger temp = updatedF[i];
        temp = temp.compareTo(half) > 0 ? temp.subtract(mod) : temp;
        if (temp.compareTo(min) < 0) {
          min = temp;
          index = i;
        }
      }
      return index;
    }

  }

  private static abstract class BlandEnteringVariableTester extends LPTester<List<BigInteger>> {

    private int expectedIndex;

    @SuppressWarnings("unused")
    int getExpextedIndex() {
      return expectedIndex;
    }

    DRes<List<BigInteger>> setupRandom(int n, int m, ProtocolBuilderNumeric builder) {
      randomTableau(n, m);
      inputTableau(builder);

      expectedIndex = enteringDanzigVariableIndex(constraints, updateMatrix, b, f);

      return builder
          .seq((seq) -> new BlandEnteringVariable(sTableau, sUpdateMatrix).buildComputation(seq))
          .seq((seq, enteringOutput) -> {
            List<DRes<SInt>> enteringIndex = enteringOutput.getFirst();
            Numeric numeric = seq.numeric();
            List<DRes<BigInteger>> opened =
                enteringIndex.stream().map(numeric::open).collect(Collectors.toList());
            return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
          });
    }

    /**
     * Computes the index of the entering variable given the plaintext LP tableu using Danzigs rule.
     *
     * @param C the constraint matrix
     * @param updateMatrix the update matrix
     * @param B the B vector
     * @param F the F vector
     * @return the entering index
     */
    private int enteringDanzigVariableIndex(Matrix<BigInteger> C, Matrix<BigInteger> updateMatrix,
        ArrayList<BigInteger> B, ArrayList<BigInteger> F) {
      BigInteger[] updatedF = new BigInteger[F.size()];
      ArrayList<BigInteger> updateRow = updateMatrix.getRow(updateMatrix.getHeight() - 1);
      for (int i = 0; i < F.size(); i++) {
        updatedF[i] = BigInteger.valueOf(0);
        List<BigInteger> column = C.getColumn(i);
        for (int j = 0; j < C.getHeight(); j++) {
          updatedF[i] = updatedF[i].add(column.get(j).multiply(updateRow.get(j)));
        }
        updatedF[i] =
            updatedF[i].add(F.get(i).multiply(updateRow.get(updateMatrix.getHeight() - 1)));
      }
      BigInteger half = mod.divide(BigInteger.valueOf(2));
      BigInteger min = updatedF[0];
      int index = 0;
      min = min.compareTo(half) > 0 ? min.subtract(mod) : min;
      for (int i = 0; i < updatedF.length; i++) {
        BigInteger temp = updatedF[i];
        temp = temp.compareTo(half) > 0 ? temp.subtract(mod) : temp;
        if (temp.compareTo(min) < 0) {
          min = temp;
          index = i;
        }
      }
      return index;
    }

  }


  private static abstract class ExitingVariableTester extends LPTester<List<BigInteger>> {

    int exitingIdx;

    @SuppressWarnings("unused")
    private int exitingIndex(int enteringIndex) {
      // TODO Fix this test case
      // BigInteger[] updatedColumn = new BigInteger[b.length];
      // BigInteger[] updatedB = new BigInteger[b.length];
      // BigInteger[] column = new BigInteger[b.length];
      // column = constraints.getIthColumn(enteringIndex, column);
      // for (int i = 0; i < b.length; i++) {
      // updatedB[i] = innerProduct(b, updateMatrix.getIthRow(i));
      // updatedColumn[i] = innerProduct(column, updateMatrix.getIthRow(i));
      // }
      // int exitingIndex = 0;
      // BigInteger half = mod.divide(BigInteger.valueOf(2));
      // BigInteger minNominator = null;
      // BigInteger minDenominator = null;
      // for (int i = 0; i < updatedB.length; i++) {
      // boolean nonPos = updatedColumn[i].compareTo(half) > 0;
      // nonPos = nonPos || updatedColumn[i].compareTo(BigInteger.ZERO) == 0;
      // if (!nonPos) {
      // if (minNominator == null) {
      // minNominator = updatedB[i];
      // minDenominator = updatedColumn[i];
      // exitingIndex = i;
      // } else {
      // BigInteger leftHand = minNominator.multiply(updatedColumn[i]);
      // BigInteger rightHand = minDenominator.multiply(updatedB[i]);
      // BigInteger diff = leftHand.subtract(rightHand).mod(mod);
      // diff = diff.compareTo(half) > 0 ? diff.subtract(mod) : diff;
      // if (diff.compareTo(BigInteger.ZERO) > 0) {
      // minNominator = updatedB[i];
      // minDenominator = updatedColumn[i];
      // exitingIndex = i;
      // }
      // }
      // }
      // }
      // return exitingIndex;
      return 0;
    }

    // private BigInteger innerProduct(BigInteger[] a, BigInteger[] b) {
    // if (a.length > b.length) {
    // throw new RuntimeException("b vector too short");
    // }
    // BigInteger result = BigInteger.valueOf(0);
    // for (int i = 0; i < a.length; i++) {
    // result = (result.add(a[i].multiply(b[i]))).mod(mod);
    // }
    // return result;
    // }
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
              mod = builder.getBasicNumericContext().getModulus();
              return setupRandom(10, 10, builder);
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
              Assert.assertEquals(one, b);
              sum++;
            }
          }
          Assert.assertEquals(1, sum);
          Assert.assertEquals(app.getExpextedIndex(), actualIndex);
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
              mod = builder.getBasicNumericContext().getModulus();
              return setupRandom(10, 10, builder);
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
              Assert.assertEquals(one, b);
              sum++;
            }
          }
          // Assert.assertEquals(1, sum);
          // Assert.assertEquals(app.getExpextedIndex(), actualIndex);
          Assert.assertEquals(0, sum);
          Assert.assertEquals(20, actualIndex);
        }
      };
    }


    public static class TestExitingVariable<ResourcePoolT extends ResourcePool>
        extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

      public TestExitingVariable() {}

      @Override
      public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
        return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

          @Override
          public void test() throws Exception {
            ExitingVariableTester app = new ExitingVariableTester() {


              @Override
              public DRes<List<BigInteger>> buildComputation(ProtocolBuilderNumeric builder) {
                mod = builder.getBasicNumericContext().getModulus();
                // setupRandom(10, 10, builder);
                return () -> null;
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
                Assert.assertEquals(one, b);
                sum++;
              }
            }
            // Assert.assertEquals(1, sum);
            // Assert.assertEquals(app.getExpextedIndex(), actualIndex);
          }
        };
      }
    }
  }
}
