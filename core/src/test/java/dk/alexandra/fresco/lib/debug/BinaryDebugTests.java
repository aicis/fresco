package dk.alexandra.fresco.lib.debug;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.bool.BooleanHelper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.Assert;

public class BinaryDebugTests {

  public static class TestBinaryOpenAndPrint<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes);

        @Override
        public void test() throws Exception {


          Application<Void, ProtocolBuilderBinary> app = producer -> producer.seq(seq -> {
            List<DRes<SBool>> toPrint =
                BooleanHelper.known(new Boolean[] {true, false, false, true}, seq.binary());
            return () -> toPrint;
          }).seq((seq, inputs) -> {
            seq.debug().openAndPrint("test", inputs, stream);
            seq.debug().marker("test", stream);
            return null;
          });
          
          
          runApplication(app);
          String output = bytes.toString("UTF-8");

          Assert.assertEquals("test\n1001\ntest\n", output.replace("\r", ""));
        }
      };
    }
  }

  public static class TestBinaryDebugToNullStream<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<Void, ProtocolBuilderBinary> app =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> toPrint =
                    BooleanHelper.known(new Boolean[] {true, false, false, true}, seq.binary());
                return () -> toPrint;
              }).seq((seq, inputs) -> {
                seq.debug().openAndPrint("test", inputs);
                seq.debug().marker("test");
                return null;
              });
              runApplication(app);
        }
      };
    }
  }

}
