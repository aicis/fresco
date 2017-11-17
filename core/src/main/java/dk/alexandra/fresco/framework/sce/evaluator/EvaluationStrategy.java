package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public enum EvaluationStrategy {
  SEQUENTIAL, SEQUENTIAL_BATCHED;

  public static <ResourcePoolT extends ResourcePool>
  BatchEvaluationStrategy<ResourcePoolT> fromString(String evalStr) throws ConfigurationException {
    EvaluationStrategy evalStrategy = EvaluationStrategy.valueOf(evalStr.toUpperCase());
    BatchEvaluationStrategy<ResourcePoolT> batchEval;
    switch (evalStrategy) {
      case SEQUENTIAL:
        batchEval = new SequentialStrategy<>();
        break;
      case SEQUENTIAL_BATCHED:
        batchEval = new BatchedStrategy<>();
        break;
      default:
        throw new ConfigurationException("Unrecognized evaluation strategy:" + evalStr);
    }
    return batchEval;
  }

  public static <ResourcePoolT extends ResourcePool>
  BatchEvaluationStrategy<ResourcePoolT> fromEnum(EvaluationStrategy strat)
      throws ConfigurationException {
    switch (strat) {
      case SEQUENTIAL:
        return new SequentialStrategy<>();
      case SEQUENTIAL_BATCHED:
        return new BatchedStrategy<>();
      default:
        throw new ConfigurationException("Unrecognized evaluation strategy:" + strat);
    }
  }
}
