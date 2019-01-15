package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.lib.collections.relational.LeakyAggregationTests;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import org.junit.Test;

public class TestSpdzRelational extends AbstractSpdzTest {

  @Test
  public void test_MiMC_aggregate_two() {
    runTest(LeakyAggregationTests.aggregate(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_MiMC_aggregate_unique_keys_two() {
    runTest(LeakyAggregationTests.aggregateUniqueKeys(),
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_MiMC_aggregate_three() {
    runTest(LeakyAggregationTests.aggregate(),
        PreprocessingStrategy.DUMMY, 3);
  }

  @Test
  public void test_MiMC_aggregate_empty() {
    runTest(LeakyAggregationTests.aggregateEmpty(),
        PreprocessingStrategy.DUMMY, 2);
  }

}
