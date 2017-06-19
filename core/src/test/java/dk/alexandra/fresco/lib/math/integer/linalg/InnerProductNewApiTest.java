package dk.alexandra.fresco.lib.math.integer.linalg;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

public class InnerProductNewApiTest {

  public static class InnerProductTest extends TestThreadFactory<ResourcePool> {

    @Override
    public TestThread next(TestThreadConfiguration<ResourcePool> conf) {
      return new TestThread() {
        int[] a = new int[] {1, 3, 5, 7, 11};
        int[] b = new int[] {2, 4, 6, 8, 10};

        @Override
        public void test() throws Exception {
          TestApplication test = new TestApplication() {

            @Override
            public ProtocolProducer prepareApplication(ProtocolFactory factory) {
              BasicNumericFactory<SInt> f = (BasicNumericFactory<SInt>) factory;
              ProtocolBuilder pb = ProtocolBuilder.createRoot(factory, seq -> {
                BasicNumericFactory<SInt> af = seq.createAppendingBasicNumericFactory();
                SInt[] sA = new SInt[a.length];
                SInt[] sB = new SInt[b.length];
                OInt c = af.getOInt();
                for (int i = 0; i < sB.length; i++) {
                  sA[i] = af.getSInt(a[i]);
                  sB[i] = af.getSInt(b[i]);
                }
                Computation<SInt> pp = new InnerProductNewApi(f, sA, sB);
                seq.append(pp);               
                seq.createSequentialSubFactory(seq2 -> {
                  BasicNumericFactory<SInt> af2 = seq2.createAppendingBasicNumericFactory();
                  af2.getOpenProtocol(pp.out(), c);
                });
                outputs = new OInt[] {c};
              });
              return pb.build();
            }
          };
          secureComputationEngine.runApplication(test, SecureComputationEngineImpl
              .createResourcePool(conf.sceConf, conf.sceConf.getSuite()));
          BigInteger b = test.outputs[0].getValue();
          Assert.assertEquals(210, b.intValue());
        }

      };
    }



  }

}
