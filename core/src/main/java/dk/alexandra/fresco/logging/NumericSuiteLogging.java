package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Collections;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.Debug;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.PreprocessedValues;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.compare.MiscBigIntegerGenerators;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.logging.arithmetic.ComparisonLoggerDecorator;
import dk.alexandra.fresco.logging.arithmetic.NumericLoggingDecorator;
import dk.alexandra.fresco.suite.ProtocolSuiteNumeric;
import java.util.Map;

/**
 * Adds logging functionality for the protocol suite, works on numeric suites.
 *
 * @param <ResourcePoolT> the resource pool of the original decorated suite.
 */
public class NumericSuiteLogging<ResourcePoolT extends ResourcePool>
    implements ProtocolSuiteNumeric<ResourcePoolT>, PerformanceLogger {

  private final ProtocolSuiteNumeric<ResourcePoolT> delegateSuite;
  private final PerformanceLoggerCountingAggregate aggregate;

  /**
   * Creates a new logging decorator for the protocol suite.
   *
   * @param protocolSuite the original protocol suite to log for.
   */
  public NumericSuiteLogging(ProtocolSuiteNumeric<ResourcePoolT> protocolSuite) {
    this.delegateSuite = protocolSuite;
    this.aggregate = new PerformanceLoggerCountingAggregate();
  }

  @Override
  public BuilderFactoryNumeric init(ResourcePoolT resourcePool,
      Network network) {
    BuilderFactoryNumeric init = delegateSuite.init(resourcePool, network);
    return new BuilderFactoryNumeric() {
      private BuilderFactoryNumeric delegateFactory = init;

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

      @Override
      public MiscBigIntegerGenerators getBigIntegerHelper() {
        return delegateFactory.getBigIntegerHelper();
      }

      @Override
      public Comparison createComparison(ProtocolBuilderNumeric builder) {
        ComparisonLoggerDecorator comparisonLoggerDecorator =
            new ComparisonLoggerDecorator(delegateFactory.createComparison(builder));
        aggregate.add(comparisonLoggerDecorator);
        return comparisonLoggerDecorator;
      }

      @Override
      public AdvancedNumeric createAdvancedNumeric(
          ProtocolBuilderNumeric builder) {
        return delegateFactory.createAdvancedNumeric(builder);
      }

      @Override
      public Collections createCollections(ProtocolBuilderNumeric builder) {
        return delegateFactory.createCollections(builder);
      }

      @Override
      public PreprocessedValues createPreprocessedValues(ProtocolBuilderNumeric builder) {
        return delegateFactory.createPreprocessedValues(builder);
      }

      @Override
      public Debug createDebug(
          ProtocolBuilderNumeric builder) {
        return delegateFactory.createDebug(builder);
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
  public Map<String, Long> getLoggedValues(int partyId) {
    return aggregate.getLoggedValues(partyId);
  }
}
