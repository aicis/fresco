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
 * THE SOFTWARE IS facIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
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
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.debug.MarkerProtocolImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import java.io.LineNumberInputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.hamcrest.core.Is;
import java.util.stream.Collectors;
import org.junit.Assert;


/**
 * Should ideally cover all protocol obtainable by a get-method in LPFactory
 * TODO That is currently not the case
 *
 */
public class LPBuildingBlockTests {

  private static abstract class LPTester extends TestApplication {

    Random rand = new Random(42);
    BigInteger mod;
    Matrix<BigInteger> updateMatrix;
    Matrix<BigInteger> constraints;
    ArrayList<BigInteger> b;
    protected ArrayList<BigInteger> f;
    LPTableau sTableau;
    Matrix<Computation<SInt>> sUpdateMatrix;

    void randomTableau(int n, int m) {
      updateMatrix = randomMatrix(m + 1, m + 1);
      constraints = randomMatrix(m, n + m);
      this.b = randomList(m);
      this.f = randomList(n + m);
    }

    void inputTableau(SequentialNumericBuilder builder) {
      builder.createParallelSub(par -> {
        NumericBuilder numeric = par.numeric();
        sTableau = new LPTableau(
            new Matrix<>(constraints.getHeight(), constraints.getWidth(),
                (i) -> toArrayList(numeric, constraints.getRow(i))),
            toArrayList(numeric, b),
            toArrayList(numeric, f),
            numeric.known(BigInteger.ZERO)
        );
        sUpdateMatrix = new Matrix<>(
            updateMatrix.getHeight(), updateMatrix.getWidth(),
            (i) -> toArrayList(numeric, updateMatrix.getRow(i)));
        return () -> null;
      });
    }

    private ArrayList<Computation<SInt>> toArrayList(NumericBuilder numeric,
        ArrayList<BigInteger> row) {
      return new ArrayList<>(row.stream().map(numeric::known)
          .collect(Collectors.toList()));
    }

    Matrix<BigInteger> randomMatrix(int n, int m) {
      return new Matrix<>(n, m,
          (i) -> randomList(m));
    }

    ArrayList<BigInteger> randomList(int m) {
      ArrayList<BigInteger> result = new ArrayList<>(m);
      while (result.size() < m) {
        result.add(new BigInteger(32, rand));
      }
      return result;
    }
  }

  private static abstract class EnteringVariableTester extends LPTester {

    private int expectedIndex;

    int getExpextedIndex() {
      return expectedIndex;
    }

    void setupRandom(int n, int m, SequentialNumericBuilder builder) {
      randomTableau(n, m);
      inputTableau(builder);

      expectedIndex = enteringDanzigVariableIndex(constraints, updateMatrix, b, f);

      builder.seq((seq) ->
          new EnteringVariable(sTableau, sUpdateMatrix).build(seq)
      ).seq((enteringOutput, seq) -> {
        List<Computation<SInt>> enteringIndex = enteringOutput.getFirst();
        NumericBuilder numeric = seq.numeric();
        List<Computation<BigInteger>> opened = enteringIndex.stream().map(numeric::open)
            .collect(Collectors.toList());
        this.outputs = opened;
        return () -> null;
      });
    }

    /**
     * Computes the index of the entering variable given the plaintext LP
     * tableu using Danzigs rule.
     *
     * @param C the constraint matrix
     * @param updateMatrix the update matrix
     * @param B the B vector
     * @param F the F vector
     * @return the entering index
     */
    private int enteringDanzigVariableIndex(Matrix<BigInteger> C, Matrix<BigInteger> updateMatrix,
        ArrayList<BigInteger> B,
        ArrayList<BigInteger> F) {
      BigInteger[] updatedF = new BigInteger[F.size()];
      ArrayList<BigInteger> updateRow = updateMatrix.getRow(updateMatrix.getHeight() - 1);
      for (int i = 0; i < F.size(); i++) {
        updatedF[i] = BigInteger.valueOf(0);
        List<BigInteger> column = C.getColumn(i);
        for (int j = 0; j < C.getHeight(); j++) {
          updatedF[i] = updatedF[i].add(column.get(j).multiply(updateRow.get(j)));
        }
        updatedF[i] = updatedF[i]
            .add(F.get(i).multiply(updateRow.get(updateMatrix.getHeight() - 1)));
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

  abstract static class ExitingTester extends LPTester {

    int exitingIdx;

    ProtocolProducer setupRandom(int n, int m, BasicNumericFactory bnf) {
      NumericIOBuilder iob = new NumericIOBuilder(bnf);
      return iob.getProtocol();
    }

    private int exitingIndex(int enteringIndex) {
      //TODO Fix this test case
//      BigInteger[] updatedColumn = new BigInteger[b.length];
//      BigInteger[] updatedB = new BigInteger[b.length];
//      BigInteger[] column = new BigInteger[b.length];
//      column = constraints.getIthColumn(enteringIndex, column);
//      for (int i = 0; i < b.length; i++) {
//        updatedB[i] = innerProduct(b, updateMatrix.getIthRow(i));
//        updatedColumn[i] = innerProduct(column, updateMatrix.getIthRow(i));
//      }
//      int exitingIndex = 0;
//      BigInteger half = mod.divide(BigInteger.valueOf(2));
//      BigInteger minNominator = null;
//      BigInteger minDenominator = null;
//      for (int i = 0; i < updatedB.length; i++) {
//        boolean nonPos = updatedColumn[i].compareTo(half) > 0;
//        nonPos = nonPos || updatedColumn[i].compareTo(BigInteger.ZERO) == 0;
//        if (!nonPos) {
//          if (minNominator == null) {
//            minNominator = updatedB[i];
//            minDenominator = updatedColumn[i];
//            exitingIndex = i;
//          } else {
//            BigInteger leftHand = minNominator.multiply(updatedColumn[i]);
//            BigInteger rightHand = minDenominator.multiply(updatedB[i]);
//            BigInteger diff = leftHand.subtract(rightHand).mod(mod);
//            diff = diff.compareTo(half) > 0 ? diff.subtract(mod) : diff;
//            if (diff.compareTo(BigInteger.ZERO) > 0) {
//              minNominator = updatedB[i];
//              minDenominator = updatedColumn[i];
//              exitingIndex = i;
//            }
//          }
//        }
//      }
//      return exitingIndex;
      return 0;
    }

    private BigInteger innerProduct(BigInteger[] a, BigInteger[] b) {
      if (a.length > b.length) {
        throw new RuntimeException("b vector too short");
      }
      BigInteger result = BigInteger.valueOf(0);
      for (int i = 0; i < a.length; i++) {
        result = (result.add(a[i].multiply(b[i]))).mod(mod);
      }
      return result;
    }
  }

  
  
  
  /*
<<<<<<< HEAD
  public static class TestRankProtocol extends TestThreadFactory {

    public TestRankProtocol() {
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = 4338818809103718010L;
            private BigInteger[] list = new BigInteger[] {
                new BigInteger("23"), new BigInteger("98"), new BigInteger("2"),
                new BigInteger("2030"), new BigInteger("2"), new BigInteger("5"),
                new BigInteger("7847"), new BigInteger("100"), new BigInteger("45")};

            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
              NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
              RandomFieldElementFactory randFactory = (RandomFieldElementFactory) factory;
              LPFactory lpFactory = new LPFactoryImpl(80, bnFactory, localInvFactory, numericBitFactory,
                  expFromOIntFactory, expFactory, randFactory);
              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              
              SInt rank = bnFactory.getSInt();
              SInt[] values = ioBuilder.inputArray(list, 1);
              SInt val = ioBuilder.input(99, 1);
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              seq.append(ioBuilder.getProtocol());
              seq.append(lpFactory.getRankProtocol(values, val, rank));
              OInt result = ioBuilder.output(rank);
              seq.append(ioBuilder.getProtocol());
              outputs = new OInt[] {result};
              return seq;
            }
            
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          Assert.assertThat(app.getOutputs()[0].getValue(), Is.is(new BigInteger("6")));
        }
      };
    }
  }

  public static class TestRankProtocolFractions extends TestThreadFactory {

    public TestRankProtocolFractions() {
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = 4338818809103718010L;

            private BigInteger[] listN = new BigInteger[] {
                new BigInteger("10"), new BigInteger("2"), new BigInteger("12"),
                new BigInteger("20"), new BigInteger("7"), new BigInteger("7"),
                new BigInteger("77"), new BigInteger("3"), new BigInteger("25")};
            
            private BigInteger[] listD = new BigInteger[] {
                new BigInteger("23"), new BigInteger("98"), new BigInteger("2"),
                new BigInteger("2030"), new BigInteger("2"), new BigInteger("5"),
                new BigInteger("7847"), new BigInteger("100"), new BigInteger("45")};
            
            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
              NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
              RandomFieldElementFactory randFactory = (RandomFieldElementFactory) factory;
              LPFactory lpFactory = new LPFactoryImpl(80, bnFactory, localInvFactory, numericBitFactory,
                  expFromOIntFactory, expFactory, randFactory);
              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              
              SInt rank = bnFactory.getSInt();
              SInt[] valuesN = ioBuilder.inputArray(listN, 1);
              SInt[] valuesD = ioBuilder.inputArray(listD, 1);
              
              SInt valN = ioBuilder.input(99, 1);
              SInt valD = ioBuilder.input(3, 1);
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              seq.append(ioBuilder.getProtocol());
              seq.append(lpFactory.getRankProtocol(valuesN, valuesD, valN, valD, rank));
              OInt result = ioBuilder.output(rank);
              seq.append(ioBuilder.getProtocol());
              outputs = new OInt[] {result};
              return seq;
            }
            
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          Assert.assertThat(app.getOutputs()[0].getValue(), Is.is(new BigInteger("9")));
        }
      };
    }
  }

  public static class TestOptimalValueProtocol extends TestThreadFactory {

    public TestOptimalValueProtocol() {
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
<<<<<<< HEAD
        private final BigInteger expected = new BigInteger("2681561585988519419914804999649525646109539430988454610970386405210600614682585741702178177297711755782023555797372489580466114588230397629716698363278143"); 
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = 4338818809103718010L;
            private int[][] matrix = new int[][] {
                {1, 2, 3, 4},
                {2, 3, 5, 6},
                {3, 4, 6, 8},
                {2, 2, 7, 2}};
            private int[] b = new int[] {2, 3, 5, 6};

            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
              NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
              RandomFieldElementFactory randFactory = (RandomFieldElementFactory) factory;
              LPFactory lpFactory = new LPFactoryImpl(80, bnFactory, localInvFactory, numericBitFactory,
                  expFromOIntFactory, expFactory, randFactory);
              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              
              SInt optimalValue = bnFactory.getSInt();
              
              SInt[][] values = ioBuilder.inputMatrix(matrix, 1);
              Matrix<SInt> updateMatrix = new Matrix<SInt>(values);
              SInt[] B = ioBuilder.inputArray(b, 1);
              SInt pivot = ioBuilder.input(5, 1);
              
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              seq.append(ioBuilder.getProtocol());
            
              seq.append(lpFactory.getOptimalValueProtocol(updateMatrix, B, pivot, optimalValue));
              OInt result = ioBuilder.output(optimalValue);
              seq.append(ioBuilder.getProtocol());
              outputs = new OInt[] {result};
              return seq;
            }
            
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          Assert.assertThat(app.getOutputs()[0].getValue(), Is.is(expected));
        }
      };
    }
  }

  public static class TestOptimalNumeratorProtocol extends TestThreadFactory {

    public TestOptimalNumeratorProtocol() {
*/      /*
      public static class TestDummy extends TestThreadFactory {
        @Override
        public void test() throws Exception {
          Application app = (Application<Void, SequentialNumericBuilder>) producer -> {
            producer.append(new MarkerProtocolImpl("Running Dummy Test"));
            return () -> null;
          };
          secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
        }
      };
    }

  }

  public static class TestDanzigEnteringVariable extends TestThreadFactory {


    public TestDanzigEnteringVariable() {
>>>>>>> develop
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
<<<<<<< HEAD
        private final BigInteger expected = new BigInteger("57");  // [2,2,7,2] x [2,3,5,6]
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

            private static final long serialVersionUID = 4338818809103718010L;
            private int[][] matrix = new int[][] {
                {1, 2, 3, 4},
                {2, 3, 5, 6},
                {3, 4, 6, 8},
                {2, 2, 7, 2}};
            private int[] b = new int[] {2, 3, 5, 6};

            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
              NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
              RandomFieldElementFactory randFactory = (RandomFieldElementFactory) factory;
              LPFactory lpFactory = new LPFactoryImpl(80, bnFactory, localInvFactory, numericBitFactory,
                  expFromOIntFactory, expFactory, randFactory);
              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              
              SInt optimalValue = bnFactory.getSInt();
              
              SInt[][] values = ioBuilder.inputMatrix(matrix, 1);
              Matrix<SInt> updateMatrix = new Matrix<SInt>(values);
              SInt[] B = ioBuilder.inputArray(b, 1);
              
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              seq.append(ioBuilder.getProtocol());
            
              seq.append(lpFactory.getOptimalNumeratorProtocol(updateMatrix, B, optimalValue));
              OInt result = ioBuilder.output(optimalValue);
              seq.append(ioBuilder.getProtocol());
              outputs = new OInt[] {result};
              return seq;
            }
            
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          Assert.assertThat(app.getOutputs()[0].getValue(), Is.is(expected));
=======
        @Override
        public void test() throws Exception {
          EnteringVariableTester app = new EnteringVariableTester() {


            @Override
            public ProtocolProducer prepareApplication(BuilderFactory factoryProducer) {
              SequentialNumericBuilder builder = ProtocolBuilderNumeric
                  .createApplicationRoot((BuilderFactoryNumeric) factoryProducer);
              mod = builder.getBasicNumericFactory().getModulus();
              setupRandom(10, 10, builder);
              return builder.build();
            }
          };
          secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
          int actualIndex = 0;
          int sum = 0;
          BigInteger zero = BigInteger.ZERO;
          BigInteger one = BigInteger.ONE;
          List<Computation<BigInteger>> outputs = app.outputs;
          for (Computation<BigInteger> b : outputs) {
            if (b.out().compareTo(zero) == 0) {
              actualIndex = (sum < 1) ? actualIndex + 1 : actualIndex;
            } else {
              Assert.assertEquals(one, b.out());
              sum++;
            }
          }
          Assert.assertEquals(1, sum);
          Assert.assertEquals(app.getExpextedIndex(), actualIndex);
>>>>>>> develop
        }
      };
    }
  }

<<<<<<< HEAD
  
  public static class TestEntryWiseProductProtocol extends TestThreadFactory {

    public TestEntryWiseProductProtocol() {
    }
=======
  public static class TestBlandEnteringVariable extends TestThreadFactory {
>>>>>>> develop

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
<<<<<<< HEAD
        private int[] expected = new int[]{4, 9, 25, 49, 81};
=======
>>>>>>> develop
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

<<<<<<< HEAD
            private static final long serialVersionUID = 4338818809103718010L;
            private int[] listA = new int[]{2, 3, 5, 7, 9};
            private int[] listB = new int[]{2, 3, 5, 7, 9};

            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
              NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
              RandomFieldElementFactory randFactory = (RandomFieldElementFactory) factory;
              LPFactory lpFactory = new LPFactoryImpl(80, bnFactory, localInvFactory, numericBitFactory,
                  expFromOIntFactory, expFactory, randFactory);
              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              
              SInt[] res = bnFactory.getSIntArray(5);
              SInt[] as = ioBuilder.inputArray(listA, 1);
              SInt[] bs = ioBuilder.inputArray(listB, 1);
              
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              seq.append(ioBuilder.getProtocol());
              seq.append(lpFactory.getEntrywiseProductProtocol(as, bs, res));
              
              outputs = ioBuilder.outputArray(res);
              seq.append(ioBuilder.getProtocol());
              return seq;
            }
            
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          for(int i = 0; i< app.getOutputs().length; i++){
            Assert.assertThat(app.getOutputs()[i].getValue(), Is.is(BigInteger.valueOf(expected[i])));  
          }
          
=======

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory producer) {
              // BasicNumericFactory fac = (BasicNumericFactory)
              // factory;
              return null;
            }
          };

          secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
>>>>>>> develop
        }
      };
    }
  }

<<<<<<< HEAD
  public static class TestEntryWiseProductProtocolOpen extends TestThreadFactory {

    public TestEntryWiseProductProtocolOpen() {
    }
=======
  public static class TestUpdateMatrix extends TestThreadFactory {
>>>>>>> develop

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
<<<<<<< HEAD
        private int[] expected = new int[]{4, 9, 25, 49, 81};
=======
>>>>>>> develop
        @Override
        public void test() throws Exception {
          TestApplication app = new TestApplication() {

<<<<<<< HEAD
            private static final long serialVersionUID = 4338818809103718010L;
            private int[] listA = new int[]{2, 3, 5, 7, 9};
            private int[] listB = new int[]{2, 3, 5, 7, 9};

            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
              NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
              RandomFieldElementFactory randFactory = (RandomFieldElementFactory) factory;
              LPFactory lpFactory = new LPFactoryImpl(80, bnFactory, localInvFactory, numericBitFactory,
                  expFromOIntFactory, expFactory, randFactory);
              NumericIOBuilder ioBuilder = new NumericIOBuilder(bnFactory);
              
              SInt[] res = bnFactory.getSIntArray(5);
              SInt[] as = ioBuilder.inputArray(listA, 1);
              
              SInt[] cbs = ioBuilder.inputArray(listB, 1);
              OInt[] bs = ioBuilder.outputArray(cbs);
              
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              seq.append(ioBuilder.getProtocol());
              seq.append(lpFactory.getEntrywiseProductProtocol(as, bs, res));
              
              outputs = ioBuilder.outputArray(res);
              seq.append(ioBuilder.getProtocol());
              return seq;
            }
            
          };
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          for(int i = 0; i< app.getOutputs().length; i++){
            Assert.assertThat(app.getOutputs()[i].getValue(), Is.is(BigInteger.valueOf(expected[i])));  
          }
          
=======

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory producer) {
              // BasicNumericFactory fac = (BasicNumericFactory)
              // factory;
              return null;
            }
          };

          secureComputationEngine
              .runApplication(app, ResourcePoolCreator.createResourcePool(conf.sceConf));
>>>>>>> develop
        }
      };
    }
  }
<<<<<<< HEAD
  
  
	private static abstract class LPTester extends TestApplication {

		Random rand = new Random(42);
		BigInteger mod;
		Matrix<BigInteger> updateMatrix;
		Matrix<BigInteger> constraints;
		BigInteger[] b;
		protected BigInteger[] f;
		LPTableau sTableau;
		Matrix<SInt> sUpdateMatrix;

		void randomTableau(int n, int m) {
			BigInteger[][] um = randomMatrix(m + 1, m + 1);
			BigInteger[][] c = randomMatrix(m, n + m);
			this.b = randomArray(m);
			this.f = randomArray(n + m);
			updateMatrix = new Matrix<>(um);
			constraints = new Matrix<>(c);
		}

		ProtocolProducer inputTableau(BasicNumericFactory bnf) {
			NumericProtocolBuilder npb = new NumericProtocolBuilder(bnf);
			NumericIOBuilder iob = new NumericIOBuilder(bnf);
			iob.beginParScope();
			SInt[] b = iob.inputArray(this.b, 1);
			SInt[] f = iob.inputArray(this.f, 1);
			SInt[][] c = iob.inputMatrix(constraints.toArray(), 1);
			SInt[][] um = iob.inputMatrix(updateMatrix.toArray(), 1);
			iob.endCurScope();
			sUpdateMatrix = new Matrix<>(um);
			Matrix<SInt> cMatrix = new Matrix<>(c);
			sTableau = new LPTableau(cMatrix, b, f, npb.getSInt());
			return iob.getProtocol();
		}

		BigInteger[][] randomMatrix(int n, int m) {
			BigInteger[][] result = new BigInteger[n][m];
			for (int i = 0; i < n; i++) {
				result[i] = randomArray(m);
			}
			return result;
		}

		BigInteger[] randomArray(int m) {
			BigInteger[] result = new BigInteger[m];
			for (int i = 0; i < m; i++) {
				result[i] = new BigInteger(32, rand);
			}
			return result;
		}
	}

	private static abstract class EnteringVariableTester extends LPTester {

		private int expectedIndex;

		int getExpextedIndex() {
			return expectedIndex;
		}

		ProtocolProducer setupRandom(int n, int m, BasicNumericFactory bnf, LPFactory lpf) {
			NumericProtocolBuilder npb = new NumericProtocolBuilder(bnf);
			NumericIOBuilder iob = new NumericIOBuilder(bnf);
			iob.beginSeqScope();
			randomTableau(n, m);
			ProtocolProducer input = inputTableau(bnf);
			iob.addProtocolProducer(input);
			expectedIndex = enteringDanzigVariableIndex(constraints, updateMatrix, b, f);
			SInt[] enteringIndex = npb.getSIntArray(n + m);
			SInt minimum = npb.getSInt();
			ProtocolProducer evc = lpf.getEnteringVariableProtocol(sTableau, sUpdateMatrix, enteringIndex, minimum);
			iob.addProtocolProducer(evc);
			this.outputs = iob.outputArray(enteringIndex);
			iob.endCurScope();
			return iob.getProtocol();
		}

		/**
		 * Computes the index of the entering variable given the plaintext LP
		 * tableu using Danzigs rule.
		 * 
		 * @param C
		 *            the constraint matrix
		 * @param updateMatrix
		 *            the update matrix
		 * @param B
		 *            the B vector
		 * @param F
		 *            the F vector
		 * @return the entering index
		 */ /*
		private int enteringDanzigVariableIndex(Matrix<BigInteger> C, Matrix<BigInteger> updateMatrix, BigInteger[] B,
				BigInteger[] F) {
			BigInteger[] updatedF = new BigInteger[F.length];
			BigInteger[] updateRow = updateMatrix.getIthRow(updateMatrix.getHeight() - 1);
			for (int i = 0; i < F.length; i++) {
				updatedF[i] = BigInteger.valueOf(0);
				BigInteger[] column = new BigInteger[C.getHeight()];
				column = C.getIthColumn(i, column);
				for (int j = 0; j < C.getHeight(); j++) {
					updatedF[i] = updatedF[i].add(column[j].multiply(updateRow[j]));
				}
				updatedF[i] = updatedF[i].add(F[i].multiply(updateRow[updateMatrix.getHeight() - 1]));
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

	
	
	public static class TestDanzigEnteringVariable extends TestThreadFactory {

		private final BigInteger mod;

		public TestDanzigEnteringVariable(BigInteger mod) {
			this.mod = mod;
		}

		@Override
		public TestThread next(TestThreadConfiguration conf) {
			return new TestThread() {
				@Override
				public void test() throws Exception {
					EnteringVariableTester app = new EnteringVariableTester() {

						private static final long serialVersionUID = 4338818809103728010L;

						@Override
						public ProtocolProducer prepareApplication(ProtocolFactory factory) {
							BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
							LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
							NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
							ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
							PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
							RandomFieldElementFactory randFactory = (RandomFieldElementFactory) factory;
							LPFactory lpFactory = new LPFactoryImpl(80, bnFactory, localInvFactory, numericBitFactory,
									expFromOIntFactory, expFactory, randFactory);
							return setupRandom(10, 10, bnFactory, lpFactory);
						}
					};
					app.mod = mod;
					secureComputationEngine
							.runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
					int actualIndex = 0;
					int sum = 0;
					BigInteger zero = BigInteger.ZERO;
					BigInteger one = BigInteger.ONE;
					for (OInt b : app.outputs) {
            System.out.println("output: "+b.getValue());
						if (b.getValue().compareTo(zero) == 0) {
							actualIndex = (sum < 1) ? actualIndex + 1 : actualIndex;
						} else {
							Assert.assertEquals(one, b.getValue());
							sum++;
						}
					}
					Assert.assertEquals(1, sum);
					Assert.assertEquals(app.getExpextedIndex(), actualIndex);
				}
			};
		}
	}
	
	public static class TestBlandEnteringVariableSolver extends TestThreadFactory {

    private final BigInteger mod;

    public TestBlandEnteringVariableSolver(BigInteger mod) {
      this.mod = mod;
    }

    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          EnteringVariableTester app = new EnteringVariableTester() {

            private static final long serialVersionUID = 4338818809103728010L;
            private int expectedIndex;

            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              BasicNumericFactory bnFactory = (BasicNumericFactory) factory;
              LocalInversionFactory localInvFactory = (LocalInversionFactory) factory;
              NumericBitFactory numericBitFactory = (NumericBitFactory) factory;
              ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) factory;
              PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) factory;
              RandomFieldElementFactory randFactory = (RandomFieldElementFactory) factory;
              LPFactory lpFactory = new LPFactoryImpl(80, bnFactory, localInvFactory, numericBitFactory,
                  expFromOIntFactory, expFactory, randFactory);
              
              
              NumericProtocolBuilder npb = new NumericProtocolBuilder(bnFactory);
              NumericIOBuilder iob = new NumericIOBuilder(bnFactory);
              iob.beginSeqScope();
              randomTableau(10, 10);
              ProtocolProducer input = inputTableau(bnFactory);
              iob.addProtocolProducer(input);
              expectedIndex = enteringDanzigVariableIndex(constraints, updateMatrix, b, f);
              SInt[] enteringIndex = npb.getSIntArray(10 + 10);
              SInt minimum = npb.getSInt();
              ProtocolProducer evc = lpFactory.getEnteringVariableProtocol(sTableau, sUpdateMatrix, enteringIndex, minimum);
              evc = new BlandEnteringVariableProtocol(sTableau, sUpdateMatrix, enteringIndex, minimum, lpFactory, bnFactory);
              iob.addProtocolProducer(evc);
              this.outputs = iob.outputArray(enteringIndex);
              iob.endCurScope();
              return iob.getProtocol();
            }
            
            
            private int enteringDanzigVariableIndex(Matrix<BigInteger> C, Matrix<BigInteger> updateMatrix, BigInteger[] B,
                BigInteger[] F) {
              BigInteger[] updatedF = new BigInteger[F.length];
              BigInteger[] updateRow = updateMatrix.getIthRow(updateMatrix.getHeight() - 1);
              for (int i = 0; i < F.length; i++) {
                updatedF[i] = BigInteger.valueOf(0);
                BigInteger[] column = new BigInteger[C.getHeight()];
                column = C.getIthColumn(i, column);
                for (int j = 0; j < C.getHeight(); j++) {
                  updatedF[i] = updatedF[i].add(column[j].multiply(updateRow[j]));
                }
                updatedF[i] = updatedF[i].add(F[i].multiply(updateRow[updateMatrix.getHeight() - 1]));
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
            
            
          };
          app.mod = mod;
          secureComputationEngine
              .runApplication(app, NetworkCreator.createResourcePool(conf.sceConf));
          int actualIndex = 0;
          int sum = 0;
          BigInteger zero = BigInteger.ZERO;
          BigInteger one = BigInteger.ONE;
          for (OInt b : app.outputs) {
            System.out.println("output: "+b.getValue());
            if (b.getValue().compareTo(zero) == 0) {
              actualIndex = (sum < 1) ? actualIndex + 1 : actualIndex;
            } else {
              Assert.assertEquals(one, b.getValue());
              sum++;
            }
          }
          //Assert.assertEquals(1, sum);
          //Assert.assertEquals(app.getExpextedIndex(), actualIndex);
        }
      };
    }
  }

//==
*/
}
