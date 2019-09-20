package dk.alexandra.fresco.lib.generic;

import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import org.junit.Assert;
import org.junit.Test;

public class BroadcastValidationProtocolTest {

  @Test(expected = IllegalStateException.class)
  public void testOutThrows() {
    new BroadcastValidationProtocol(new byte[1]).out();
  }

  @Test
  public void testEvaluateWithTwoPartiesReturnsDone() {
    EvaluationStatus status = new BroadcastValidationProtocol<>(new byte[1])
        .evaluate(0, new ResourcePool() {
          @Override
          public int getMyId() {
            return 0;
          }

          @Override
          public int getNoOfParties() {
            return 2;
          }
        }, null);
    Assert.assertEquals(EvaluationStatus.IS_DONE, status);
  }
}
