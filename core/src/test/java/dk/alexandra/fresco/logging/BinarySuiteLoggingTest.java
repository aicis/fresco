package dk.alexandra.fresco.logging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuiteBinary;
import org.junit.Test;
import org.mockito.Mock;

public class BinarySuiteLoggingTest {

  @Mock public ProtocolSuiteBinary<ResourcePool> delegateSuite;

  @Test
  public void reset() {
    PerformanceLoggerCountingAggregate aggregate = mock(PerformanceLoggerCountingAggregate.class);
    BinarySuiteLogging<ResourcePool> logging = new BinarySuiteLogging<>(delegateSuite,aggregate);
    logging.reset();

    verify(aggregate, times(1)).reset();
  }
}
