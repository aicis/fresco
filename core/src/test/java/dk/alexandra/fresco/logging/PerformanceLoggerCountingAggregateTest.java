package dk.alexandra.fresco.logging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class PerformanceLoggerCountingAggregateTest {

  @Test
  public void reset() {
    PerformanceLogger logger = mock(PerformanceLogger.class);

    PerformanceLoggerCountingAggregate aggregate = new PerformanceLoggerCountingAggregate();
    aggregate.add(logger);
    aggregate.reset();

    verify(logger, times(1)).reset();
  }
}
