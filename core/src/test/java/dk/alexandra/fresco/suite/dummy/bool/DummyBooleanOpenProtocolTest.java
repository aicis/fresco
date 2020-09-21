package dk.alexandra.fresco.suite.dummy.bool;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;
import org.junit.Assert;
import org.junit.Test;

public class DummyBooleanOpenProtocolTest {

  @Test
  public void evaluateTargetEqualId() {
    DRes<SBool> closed = mock(DRes.class);
    DummyBooleanSBool closedOut = mock(DummyBooleanSBool.class);

    when(closed.out()).thenReturn(closedOut);
    when(closedOut.getValue()).thenReturn(true);

    ResourcePool resourcePool = mock(ResourcePool.class);
    when(resourcePool.getMyId()).thenReturn(2);

    DummyBooleanOpenProtocol protocol = new DummyBooleanOpenProtocol(closed, 2);
    Assert.assertEquals(protocol.evaluate(1, resourcePool, mock(Network.class)), EvaluationStatus.IS_DONE);

    Assert.assertTrue(protocol.out());
  }
  @Test
  public void evaluateTargetDifferentId() {
    DRes<SBool> closed = mock(DRes.class);
    DummyBooleanSBool closedOut = mock(DummyBooleanSBool.class);

    when(closed.out()).thenReturn(closedOut);
    when(closedOut.getValue()).thenReturn(true);

    ResourcePool resourcePool = mock(ResourcePool.class);
    when(resourcePool.getMyId()).thenReturn(1);

    DummyBooleanOpenProtocol protocol = new DummyBooleanOpenProtocol(closed, 2);
    Assert.assertEquals(protocol.evaluate(1, resourcePool, mock(Network.class)), EvaluationStatus.IS_DONE);

    Assert.assertNull(protocol.out());
  }
}
