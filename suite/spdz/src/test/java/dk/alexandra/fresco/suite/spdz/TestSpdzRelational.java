package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.collections.relational.LeakyAggregationTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzRelational extends AbstractSpdzTest {

  @Test
  public void test_MiMC_aggregate_two() throws Exception {
    runTest(LeakyAggregationTests.aggregate(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }
  
  @Test
  public void test_MiMC_aggregate_unique_keys_two() throws Exception {
    runTest(LeakyAggregationTests.aggregateUniqueKeys(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_MiMC_aggregate_three() throws Exception {
    runTest(LeakyAggregationTests.aggregate(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 3);
  }

  @Test
  public void test_MiMC_aggregate_empty() throws Exception {
    runTest(LeakyAggregationTests.aggregateEmpty(), EvaluationStrategy.SEQUENTIAL,
        PreprocessingStrategy.DUMMY, 2);
  }
  
}
