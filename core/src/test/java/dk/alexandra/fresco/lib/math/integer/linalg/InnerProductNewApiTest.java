package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.OpenBuilder;
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
              ProtocolBuilder pb = ProtocolBuilder.createRoot(factoryNumeric, seq -> {
                SInt[] sA = new SInt[a.length];
                SInt[] sB = new SInt[b.length];
                for (int i = 0; i < sB.length; i++) {
                  sA[i] = seq.getSIntFactory().getSInt(a[i]);
                  sB[i] = seq.getSIntFactory().getSInt(b[i]);
                }
                Computation<SInt> innerProduct = seq
                    .append(new InnerProductNewApi(factoryNumeric, sA, sB));
                seq.createSequentialSubFactory(seq2 -> {
                  OpenBuilder af2 = seq2.createOpenBuilder();
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
