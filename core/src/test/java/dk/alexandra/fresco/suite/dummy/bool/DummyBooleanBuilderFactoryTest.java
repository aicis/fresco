package dk.alexandra.fresco.suite.dummy.bool;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;
import org.junit.Assert;
import org.junit.Test;

public class DummyBooleanBuilderFactoryTest {

  private DummyBooleanOpenProtocol result;

  @Test
  public void createBinary() {
    ProtocolBuilderBinary builder = mock(ProtocolBuilderBinary.class);
    DRes<SBool> closed = mock(DRes.class);
    DummyBooleanSBool closedOut = mock(DummyBooleanSBool.class);

    when(closed.out()).thenReturn(closedOut);
    when(closedOut.getValue()).thenReturn(true);
    when(builder.append(any(DummyBooleanOpenProtocol.class)))
        .then(
            invocationOnMock -> {
              result = invocationOnMock.getArgument(0);
              return null;
            });

    Binary binary = new DummyBooleanBuilderFactory().createBinary(builder);
    binary.open(closed, 2);

    ResourcePool resourcePool = mock(ResourcePool.class);
    when(resourcePool.getMyId()).thenReturn(2);

    Assert.assertNotNull(result);
    Assert.assertNull(result.out());
    Assert.assertEquals(
        result.evaluate(1, resourcePool, mock((Network.class))), EvaluationStatus.IS_DONE);
    Assert.assertTrue(result.out());
  }
}
