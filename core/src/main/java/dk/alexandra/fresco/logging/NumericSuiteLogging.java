package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.logging.arithmetic.NumericLoggingDecorator;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import java.util.Map;

/**
 * Adds logging functionality for the protocol suite, works on numeric suites.
 *
 * @param <ResourcePoolT> the resource pool of the original decorated suite.
 */
public class NumericSuiteLogging<ResourcePoolT extends NumericResourcePool>
    implements ProtocolSuiteNumeric<ResourcePoolT>, PerformanceLogger {

  private final ProtocolSuiteNumeric<ResourcePoolT> delegateSuite;
  private final PerformanceLoggerCountingAggregate aggregate;

  /**
   * Creates a new logging decorator for the protocol suite.
   *
   * @param protocolSuite the original protocol suite to log for.
   */
  public NumericSuiteLogging(ProtocolSuiteNumeric<ResourcePoolT> protocolSuite) {
    this(protocolSuite, new PerformanceLoggerCountingAggregate());
  }

  protected NumericSuiteLogging(ProtocolSuiteNumeric<ResourcePoolT> protocolSuite,
      PerformanceLoggerCountingAggregate aggregate) {
    this.delegateSuite = protocolSuite;
    this.aggregate = aggregate;

  }

  @Override
  public BuilderFactoryNumeric init(ResourcePoolT resourcePool) {
    final BuilderFactoryNumeric delegateFactory = delegateSuite.init(resourcePool);
    return new BuilderFactoryNumeric() {

      @Override
      public BasicNumericContext getBasicNumericContext() {
        return delegateFactory.getBasicNumericContext();
      }

      @Override
      public Numeric createNumeric(ProtocolBuilderNumeric builder) {
        NumericLoggingDecorator numericLoggingDecorator =
            new NumericLoggingDecorator(delegateFactory.createNumeric(builder));
        aggregate.add(numericLoggingDecorator);
        return numericLoggingDecorator;
      }
    };
  }

  @Override
  public RoundSynchronization<ResourcePoolT> createRoundSynchronization() {
    return delegateSuite.createRoundSynchronization();
  }

  @Override
  public void reset() {
    aggregate.reset();
  }

  @Override
  public Map<String, Long> getLoggedValues() {
    return aggregate.getLoggedValues();
  }
}
