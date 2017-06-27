package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;

public class InnerProductNewApiTest {

  public static class InnerProductTest extends TestThreadFactory<ResourcePool> {

    @Override
    public TestThread next(TestThreadConfiguration<ResourcePool> conf) {
      return new TestThread() {
        int[] a = new int[]{1, 3, 5, 7, 11};
        int[] b = new int[]{2, 4, 6, 8, 10};
        List<Computation<OInt>> output = new ArrayList<>();

        @Override
        public void test() throws Exception {
          TestApplication test = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(BuilderFactory producer) {
              BuilderFactoryNumeric factoryNumeric = (BuilderFactoryNumeric) producer;
              ProtocolBuilder pb = ProtocolBuilder.createApplicationRoot(factoryNumeric, seq -> {
                List<Computation<SInt>> sA = new ArrayList<>(a.length);
                List<Computation<SInt>> sB = new ArrayList<>(b.length);
                for (int i = 0; i < b.length; i++) {
                  sA.add(seq.numeric().known(BigInteger.valueOf(a[i])));
                  sB.add(seq.numeric().known(BigInteger.valueOf(b[i])));
                }
                //Sub scope needed since the InnerProductNewApi needs the actual SInt
                Computation<SInt> innerProduct = seq.createSequentialSub(
                    innerSeq ->
                        innerSeq.append(
                            new InnerProductNewApi(
                                factoryNumeric,
                                sA.stream().map(Computation::out).toArray(SInt[]::new),
                                sB.stream().map(Computation::out).toArray(SInt[]::new))
                        )
                );
                seq.createIteration(seq2 -> {
                  NumericBuilder af2 = seq2.numeric();
                  output.add(af2.open(innerProduct));
                });
              });
              return pb.build();
            }
          };
          secureComputationEngine.runApplication(test, SecureComputationEngineImpl
              .createResourcePool(conf.sceConf, conf.sceConf.getSuite()));
          BigInteger b = output.get(0).out().getValue();
          Assert.assertEquals(210, b.intValue());
        }

      };
    }


  }

}
