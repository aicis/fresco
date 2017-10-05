package dk.alexandra.fresco.framework.sce;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class SCEFactory {

  /**
   * Generates an SecureComputationEngine based on the given configuration. This will still assume
   * that protocol suite configuration is loaded via property files. If this
   * is not wanted, use the factory method
   * {@code SCEFactory.getSCEFromConfiguration(SCEConfiguration conf,
   * ProtocolSuiteConfiguration psConf} instead.
   */
  public static <
      ResourcePoolT extends ResourcePool,
      Builder extends ProtocolBuilder
      >
      SecureComputationEngine<ResourcePoolT, Builder> getSCEFromConfiguration(
      ProtocolSuite<ResourcePoolT, Builder> protocolSuite,
      ProtocolEvaluator<ResourcePoolT, Builder> evaluator) {
    return new SecureComputationEngineImpl<>(protocolSuite, evaluator);
  }
}
