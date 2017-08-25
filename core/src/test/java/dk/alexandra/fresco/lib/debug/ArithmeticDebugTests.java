package dk.alexandra.fresco.lib.debug;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.lp.Matrix;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class ArithmeticDebugTests {

  public static class TestArithmeticOpenAndPrint<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderNumeric> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes);

        @Override
        public void test() throws Exception {
          Application<Void, ProtocolBuilderNumeric> app =
              new Application<Void, ProtocolBuilderNumeric>() {

                @Override
                public Computation<Void> prepareApplication(ProtocolBuilderNumeric producer) {
                  return producer.seq(seq -> {
                    NumericBuilder numeric = seq.numeric();
                    List<Computation<SInt>> toPrint =
                        Arrays.stream(
                            new BigInteger[]{BigInteger.ONE, BigInteger.TEN, BigInteger.ZERO,
                                BigInteger.ONE}
                        ).map((n) -> numeric.input(n, 1)).collect(Collectors.toList());
                    return () -> toPrint;
                  }).seq((inputs, seq) -> {
                    seq.utility().openAndPrint("testNumber", inputs.get(0), stream);
                    seq.utility().openAndPrint("testVector", inputs, stream);
                    ArrayList<Computation<SInt>> r1 = new ArrayList<>();
                    r1.add(inputs.get(0));
                    r1.add(inputs.get(1));
                    ArrayList<Computation<SInt>> r2 = new ArrayList<>();
                    r2.add(inputs.get(2));
                    r2.add(inputs.get(3));
                    ArrayList<ArrayList<Computation<SInt>>> m = new ArrayList<>();
                    m.add(r1);
                    m.add(r2);
                    Matrix<Computation<SInt>> matrix = new Matrix<>(2, 2, m);
                    seq.utility().openAndPrint("testMatrix", matrix, stream);
                    return null;
                  });
                }

              };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          String output = bytes.toString("UTF-8");
          Assert.assertEquals(
              "testNumber\n1\ntestVector\n1, 10, 0, 1, \ntestMatrix\n1, 10, \n0, 1, \n\n",
              output.replace("\r", ""));
        }
      };
    }
  }
}
