package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public enum EvaluationStrategy {
  SEQUENTIAL, SEQUENTIAL_BATCHED;

  public static <ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> ProtocolEvaluator<ResourcePoolT, Builder> fromString(
      String evalStr) throws ConfigurationException {
    EvaluationStrategy evalStrategy = EvaluationStrategy.valueOf(evalStr.toUpperCase());
    switch (evalStrategy) {
      case SEQUENTIAL:
        return new SequentialEvaluator<ResourcePoolT, Builder>();
      case SEQUENTIAL_BATCHED:
        return new BatchedSequentialEvaluator<ResourcePoolT, Builder>();
      default:
        throw new ConfigurationException("Unrecognized evaluation strategy:" + evalStr);
    }
  }

  public static <ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> ProtocolEvaluator<ResourcePoolT, Builder> fromEnum(
      EvaluationStrategy strat) throws ConfigurationException {
    switch (strat) {
      case SEQUENTIAL:
        return new SequentialEvaluator<ResourcePoolT, Builder>();
      case SEQUENTIAL_BATCHED:
        return new BatchedSequentialEvaluator<ResourcePoolT, Builder>();
      default:
        throw new ConfigurationException("Unrecognized evaluation strategy:" + strat);
    }
  }
}
