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
}
