package dk.alexandra.fresco.logging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.List;
import org.junit.Test;
import org.mockito.Mock;

public class EvaluatorLoggingDecoratorTest {

  @Mock public ProtocolEvaluator<ResourcePool> delegate;

  @Test
  public void reset() {
    List<Long> logger = mock(List.class);
    EvaluatorLoggingDecorator<ResourcePool, ProtocolBuilder> decorator =
        new EvaluatorLoggingDecorator<>(delegate, logger);
    decorator.reset();

    verify(logger, times(1)).clear();
  }
}
