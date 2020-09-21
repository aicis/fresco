package dk.alexandra.fresco.logging;

import static org.mockito.Mockito.mock;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.sce.evaluator.BatchEvaluationStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BatchEvaluationLoggingDecoratorTest {

  @Mock public BatchEvaluationStrategy<ResourcePool> delegate;
  @Mock public ProtocolCollection<ResourcePool> collection;

  @Test
  public void reset() {
    BatchEvaluationLoggingDecorator<ResourcePool> decorator =
        new BatchEvaluationLoggingDecorator<>(delegate);

    decorator.processBatch(collection, mock(ResourcePool.class), mock(NetworkBatchDecorator.class));
    Assert.assertEquals(1, decorator.getLoggedValues().get("AMOUNT_OF_BATCHES").intValue());
    decorator.reset();
    Assert.assertEquals(0, decorator.getLoggedValues().get("AMOUNT_OF_BATCHES").intValue());
  }
}
