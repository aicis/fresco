package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.sce.resources.ResourcePool;

public enum EvaluationStrategy {
  SEQUENTIAL {
    @Override
    public <ResourcePoolT extends ResourcePool>
        BatchEvaluationStrategy<ResourcePoolT> getStrategy() {
      return new SequentialStrategy<>();
    }
  }, SEQUENTIAL_BATCHED_OLD {
    @Override
    public <ResourcePoolT extends ResourcePool>
        BatchEvaluationStrategy<ResourcePoolT> getStrategy() {
      return new BatchedStrategy<>();
    }
  }, SEQUENTIAL_BATCHED {
    @Override
    public <ResourcePoolT extends ResourcePool>
    BatchEvaluationStrategy<ResourcePoolT> getStrategy() {
      return new NativeBatchedStrategy<>();
    }
  };

  public abstract <ResourcePoolT extends ResourcePool>
      BatchEvaluationStrategy<ResourcePoolT> getStrategy();
}
