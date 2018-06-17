package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.lp.LPSolver.PivotRule;
import dk.alexandra.fresco.lib.statistics.DeaSolver.AnalysisType;
import dk.alexandra.fresco.lib.statistics.DeaSolver.DeaResult;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.hamcrest.core.IsNull;
import org.junit.Assert;

/**
 * Test class for the DEASolver. Will generate a random data sample and perform a Data Envelopment
 * Analysis on it. The TestDEADSolver takes the size of the problem as inputs (i.e. the number of
 * input and output variables, the number of rows in the basis and the number of queries to perform.
 * The MPC result is compared with the result of a plaintext DEA solver.
 */
public class DeaSolverTests {

  public static class RandomDataDeaTest<ResourcePoolT extends ResourcePool>
      extends TestDeaSolver<ResourcePoolT> {

    private static final int BIT_LENGTH = 9;

    public RandomDataDeaTest(int inputVariables, int outputVariables, int rows, int queries,
        AnalysisType type) {
      this(inputVariables, outputVariables, rows, queries, type, new Random(2));
    }

    private RandomDataDeaTest(int inputVariables, int outputVariables, int rows, int queries,
        AnalysisType type, Random rand) {
      this(inputVariables, outputVariables, rows, queries, type, rand, 50, false);
    }

    public RandomDataDeaTest(int inputVariables, int outputVariables, int rows, int queries,
        AnalysisType type, Random rand, int maxNoOfIterations, boolean willBreak) {
      super(randomMatrix(rows, inputVariables, rand), randomMatrix(rows, outputVariables, rand),
          randomMatrix(queries, inputVariables, rand), randomMatrix(queries, outputVariables, rand),
          type, maxNoOfIterations, willBreak);
    }

    private static List<List<BigInteger>> randomMatrix(int width, int height, Random rand) {
      List<List<BigInteger>> result = new ArrayList<>();
      for (int i = 0; i < width; i++) {
        ArrayList<BigInteger> row = new ArrayList<>();
        for (int j = 0; j < height; j++) {
          row.add(new BigInteger(BIT_LENGTH, rand));
        }
        result.add(row);
      }
      return result;
    }
  }

  public static class TestDeaFixed1<ResourcePoolT extends ResourcePool>
      extends TestDeaSolver<ResourcePoolT> {

    private static List<List<BigInteger>> inputs;
    private static List<List<BigInteger>> outputs;

    static {
      int[][] dataSet1 = new int[][]{new int[]{29, 13451, 14409, 16477}, // Score 1
          new int[]{2, 581, 531, 1037}, // Score 1
          new int[]{26, 13352, 1753, 13528}, // Score 1
          new int[]{15, 4828, 949, 5126}, // Score 0.9857962644001192
          new int[]{20, 6930, 6376, 9680} //
      };
      inputs = buildInputs(dataSet1);
      outputs = buildOutputs(dataSet1);
    }

    public TestDeaFixed1(AnalysisType type) {
      super(inputs, outputs, inputs, outputs, type, 50, false);
    }
  }

  public static class TestDeaFixed2<ResourcePoolT extends ResourcePool>
      extends TestDeaSolver<ResourcePoolT> {

    private static List<List<BigInteger>> inputs;
    private static List<List<BigInteger>> outputs;

    static {
      int[][] dataset = new int[][]{new int[]{10, 20, 30, 1000}, new int[]{5, 10, 15, 1000},
          new int[]{200, 300, 400, 100}};
      inputs = buildInputs(dataset);
      outputs = buildOutputs(dataset);
    }


    public TestDeaFixed2(AnalysisType type) {
      super(inputs, outputs, inputs, outputs, type, 50, false);
    }
  }

  private static List<List<BigInteger>> buildInputs(int[][] dataset) {
    List<List<BigInteger>> inputs = new ArrayList<>();
    for (int i = 0; i < dataset.length; i++) {
      inputs.add(new ArrayList<>());
      for (int j = 0; j < dataset[0].length - 1; j++) {
        inputs.get(i).add(BigInteger.valueOf(dataset[i][j]));
      }
    }
    return inputs;
  }

  private static List<List<BigInteger>> buildOutputs(int[][] dataset) {
    List<List<BigInteger>> outputs = new ArrayList<>();
    for (int i = 0; i < dataset.length; i++) {
      outputs.add(new ArrayList<>());
      outputs.get(i).add(BigInteger.valueOf(dataset[i][dataset[i].length - 1]));
    }
    return outputs;
  }

  public static class TestDeaSolver<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final List<List<BigInteger>> rawTargetOutputs;
    private final List<List<BigInteger>> rawTargetInputs;
    private final List<List<BigInteger>> rawBasisOutputs;
    private final List<List<BigInteger>> rawBasisInputs;
    private final AnalysisType type;
    private final int maxNoOfIterations;
    private boolean willBreak;

    public TestDeaSolver(List<List<BigInteger>> rawBasisInputs,
        List<List<BigInteger>> rawBasisOutputs, List<List<BigInteger>> rawTargetInputs,
        List<List<BigInteger>> rawTargetOutputs, AnalysisType type, int maxNoOfIterations,
        boolean willBreak) {
      this.rawTargetOutputs = rawTargetOutputs;
      this.rawTargetInputs = rawTargetInputs;
      this.rawBasisOutputs = rawBasisOutputs;
      this.rawBasisInputs = rawBasisInputs;
      this.type = type;
      this.maxNoOfIterations = maxNoOfIterations;
      this.willBreak = willBreak;
    }


    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        private BigInteger modulus;

        @Override
        public void test() {

          Application<DeaSolver, ProtocolBuilderNumeric> app = producer -> {
            modulus = producer.getBasicNumericContext().getModulus();
            Numeric numeric = producer.numeric();
            List<List<BigInteger>> rawTargetOutputs = TestDeaSolver.this.rawTargetOutputs;
            List<List<DRes<SInt>>> targetOutputs = knownMatrix(numeric, rawTargetOutputs);
            List<List<DRes<SInt>>> targetInputs = knownMatrix(numeric, rawTargetInputs);
            List<List<DRes<SInt>>> basisOutputs = knownMatrix(numeric, rawBasisOutputs);
            List<List<DRes<SInt>>> basisInputs = knownMatrix(numeric, rawBasisInputs);
            return () -> new DeaSolver(PivotRule.DANZIG, type, targetInputs, targetOutputs,
                basisInputs,
                basisOutputs, maxNoOfIterations);
          };
          DeaSolver solver = runApplication(app);

          List<DeaResult> deaResults = runApplication(solver);

          if (willBreak) {
            deaResults.forEach(result -> Assert.assertThat(result.optimal, IsNull.nullValue()));
            return;
          }

          Application<List<OpenDeaResult>, ProtocolBuilderNumeric> app2 =
              producer -> {
                Numeric numeric = producer.numeric();
                List<DRes<OpenDeaResult>> result =
                    new ArrayList<>();
                for (DeaResult deaResult : deaResults) {
                  result.add(OpenDeaResult.createOpenDeaResult(numeric, deaResult));
                }
                return () -> result.stream().map(DRes::out).collect(Collectors.toList());
              };
          List<OpenDeaResult> openResults = runApplication(app2);

          // Solve the problem using a plaintext solver
          PlaintextDEASolver plainSolver = new PlaintextDEASolver();
          plainSolver.addBasis(asArray(rawBasisInputs), asArray(rawBasisOutputs));

          double[] plain =
              plainSolver.solve(asArray(rawTargetInputs), asArray(rawTargetOutputs), type);

          int lambdas = (type == AnalysisType.INPUT_EFFICIENCY) ? rawBasisInputs.size()
              : rawBasisInputs.size() + 1;

          for (int i = 0; i < rawTargetInputs.size(); i++) {
            OpenDeaResult deaResult = openResults.get(i);
            Assert.assertEquals(plain[i], postProcess(deaResult.optimal, type, modulus), 0.0000001);

            double computedScoreFromValues =
                deaResult.numerator.doubleValue() / deaResult.denominator.doubleValue();
            if (type == DeaSolver.AnalysisType.INPUT_EFFICIENCY) {
              computedScoreFromValues *= -1;
            }
            Assert.assertEquals(plain[i], computedScoreFromValues, 0.0000001);

            List<BigInteger> peers = deaResult.peers;
            List<BigInteger> peerValues = deaResult.peerValues;
            double sum = 0;
            for (BigInteger b : peerValues) {
              sum += postProcess(b, AnalysisType.OUTPUT_EFFICIENCY, modulus);
            }
            Assert.assertEquals("Peer values summed to " + sum + " instead of 1", 1, sum, 0.000001);
            for (BigInteger b : peers) {
              int idx = b.intValue();
              Assert.assertTrue("Peer index" + idx + ", was larger than " + (lambdas - 1),
                  idx < lambdas);
            }
            // Check input constraints are satisfied
            for (int k = 0; k < rawTargetInputs.get(i).size(); k++) {
              List<BigInteger> constraintRow = getConstraintRow(k, rawBasisInputs);
              double leftHand = 0;
              for (int l = 0; l < constraintRow.size(); l++) {
                int idx = peers.indexOf(BigInteger.valueOf(l));
                if (idx > -1) {
                  double peerValue =
                      postProcess(peerValues.get(idx), AnalysisType.OUTPUT_EFFICIENCY, modulus);
                  double constraintValue = constraintRow.get(l).doubleValue();
                  double prod = peerValue * constraintValue;
                  leftHand += prod;
                }
              }
              double rightHand;
              if (type == AnalysisType.INPUT_EFFICIENCY) {
                rightHand = rawTargetInputs.get(i).get(k).doubleValue() * plain[i];
              } else {
                rightHand = rawTargetInputs.get(i).get(k).doubleValue();
              }
              Assert.assertTrue((leftHand - rightHand) < 0.000001);
            }
            // Check output constraints are satisfied
            for (int k = 0; k < rawTargetOutputs.get(i).size(); k++) {
              List<BigInteger> constraintRow = getConstraintRow(k, rawBasisOutputs);
              if (type == AnalysisType.OUTPUT_EFFICIENCY) {
                constraintRow.add(rawTargetOutputs.get(i).get(k));
              }
              double leftHand = 0;
              for (int l = 0; l < constraintRow.size(); l++) {
                int idx = peers.indexOf(BigInteger.valueOf(l));
                if (idx > -1) {
                  double peerValue =
                      postProcess(peerValues.get(idx), AnalysisType.OUTPUT_EFFICIENCY, modulus);
                  double constraintValue = constraintRow.get(l).doubleValue();
                  double prod = peerValue * constraintValue;
                  leftHand += prod;
                }
              }
              double rightHand;
              if (type == AnalysisType.INPUT_EFFICIENCY) {
                rightHand = rawTargetOutputs.get(i).get(k).doubleValue();
              } else {
                rightHand = rawTargetOutputs.get(i).get(k).doubleValue() * plain[i];
              }
              Assert.assertTrue((rightHand - leftHand) < 0.000001);
            }
          }
        }
      };
    }

    private List<BigInteger> getConstraintRow(int i, List<List<BigInteger>> basisData) {
      ArrayList<BigInteger> constraintRow = new ArrayList<>();
      for (List<BigInteger> l : basisData) {
        constraintRow.add(l.get(i));
      }
      return constraintRow;
    }

    private BigInteger[][] asArray(List<List<BigInteger>> lists) {
      return lists.stream().map(list -> list.toArray(new BigInteger[list.size()]))
          .toArray(BigInteger[][]::new);
    }

    private static class OpenDeaResult {

      private final BigInteger optimal;
      private final BigInteger numerator;
      private final BigInteger denominator;
      private final List<BigInteger> peers;
      private final List<BigInteger> peerValues;

      private OpenDeaResult(
          BigInteger optimal,
          BigInteger numerator,
          BigInteger denominator,
          List<BigInteger> peers,
          List<BigInteger> peerValues) {
        this.optimal = optimal;
        this.numerator = numerator;
        this.denominator = denominator;
        this.peers = peers;
        this.peerValues = peerValues;
      }

      static DRes<OpenDeaResult> createOpenDeaResult(Numeric numeric, DeaResult deaResult) {
        DRes<BigInteger> optimal = numeric.open(deaResult.optimal);
        DRes<BigInteger> numerator = numeric.open(deaResult.numerator);
        DRes<BigInteger> denominator = numeric.open(deaResult.denominator);
        List<DRes<BigInteger>> peers =
            deaResult.peers.stream().map(numeric::open).collect(Collectors.toList());
        List<DRes<BigInteger>> peerValues =
            deaResult.peerValues.stream().map(numeric::open).collect(Collectors.toList());
        return () -> new OpenDeaResult(
            optimal.out(),
            numerator.out(),
            denominator.out(),
            peers.stream().map(DRes::out).collect(Collectors.toList()),
            peerValues.stream().map(DRes::out).collect(Collectors.toList())
        );
      }
    }
  }

  private static List<List<DRes<SInt>>> knownMatrix(Numeric numeric,
      List<List<BigInteger>> rawTargetOutputs) {
    return rawTargetOutputs.stream()
        .map(list -> list.stream().map(numeric::known).collect(Collectors.toList()))
        .collect(Collectors.toList());
  }

  /**
   * Reduces a field-element to a double using Gauss reduction.
   */
  private static double postProcess(BigInteger input, AnalysisType type, BigInteger modulus) {
    BigInteger[] gauss = gauss(input, modulus);
    double res = (gauss[0].doubleValue() / gauss[1].doubleValue());
    if (type == DeaSolver.AnalysisType.INPUT_EFFICIENCY) {
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
   *     <i>2r*s < N</i>.
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
