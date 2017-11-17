package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public enum EvaluationStrategy {
  SEQUENTIAL {
    @Override
    public <ResourcePoolT extends ResourcePool>
    BatchEvaluationStrategy<ResourcePoolT> getStrategy() {
      return new SequentialStrategy<>();
    }
  }, SEQUENTIAL_BATCHED {
    @Override
    public <ResourcePoolT extends ResourcePool>
    BatchEvaluationStrategy<ResourcePoolT> getStrategy() {
      return new BatchedStrategy<>();
    }
  };

  public abstract <ResourcePoolT extends ResourcePool>
  BatchEvaluationStrategy<ResourcePoolT> getStrategy();

  public static <ResourcePoolT extends ResourcePool>
  BatchEvaluationStrategy<ResourcePoolT> fromString(String evalStr) {
    EvaluationStrategy evalStrategy = EvaluationStrategy.valueOf(evalStr.toUpperCase());
    return evalStrategy.getStrategy();
  }
}
