package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.logging.binary.BinaryLoggingDecorator;
import dk.alexandra.fresco.suite.ProtocolSuiteBinary;
import java.util.Map;

/**
 * Adds logging functionality for the protocol suite, works on binary suites.
 *
 * @param <ResourcePoolT> the resource pool of the original decorated suite.
 */
public class BinarySuiteLogging<ResourcePoolT extends ResourcePool>
    implements ProtocolSuiteBinary<ResourcePoolT>, PerformanceLogger {

  private final ProtocolSuiteBinary<ResourcePoolT> delegateSuite;
  private final PerformanceLoggerCountingAggregate aggregate;

  /**
   * Creates a new logging decorator for the protocol suite.
   *
   * @param protocolSuite the original protocol suite to log for.
   */
  public BinarySuiteLogging(ProtocolSuiteBinary<ResourcePoolT> protocolSuite) {
    this(protocolSuite,new PerformanceLoggerCountingAggregate());
  }

  protected BinarySuiteLogging(ProtocolSuiteBinary<ResourcePoolT> protocolSuite,
      PerformanceLoggerCountingAggregate aggregate) {
    this.delegateSuite = protocolSuite;
    this.aggregate = aggregate;
  }

  @Override
  public BuilderFactoryBinary init(ResourcePoolT resourcePool) {
    BuilderFactoryBinary init = delegateSuite.init(resourcePool);
    return new BuilderFactoryBinary() {

      BuilderFactoryBinary delegateFactory = init;
      
      @Override
      public Binary createBinary(ProtocolBuilderBinary builder) {
        BinaryLoggingDecorator binaryLogger = 
            new BinaryLoggingDecorator(delegateFactory.createBinary(builder));
        aggregate.add(binaryLogger);
        return binaryLogger;
      }
      
//      @Override
//      public Comparison createComparison(ProtocolBuilderBinary builder) {
//        BinaryComparisonLoggingDecorator comparison =
//            new BinaryComparisonLoggingDecorator(delegateFactory.createComparison(builder));
//        aggregate.add(comparison);
//        return comparison;
//      }
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
