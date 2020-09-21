package dk.alexandra.fresco.logging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import org.junit.Test;
import org.mockito.Mock;

public class NumericSuiteLoggingTest {

  @Mock public ProtocolSuiteNumeric<NumericResourcePool> delegateSuite;

  @Test
  public void reset() {
    PerformanceLoggerCountingAggregate aggregate = mock(PerformanceLoggerCountingAggregate.class);

    NumericSuiteLogging<NumericResourcePool> logging =
        new NumericSuiteLogging<>(delegateSuite, aggregate);
    logging.reset();

    verify(aggregate, times(1)).reset();
  }
}
