package dk.alexandra.fresco.framework.sce.evaluator;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestEvaluationStrategy {

 @Test
 public void testEnums(){
   assertThat(EvaluationStrategy.valueOf("SEQUENTIAL"), is(EvaluationStrategy.SEQUENTIAL));
   assertThat(EvaluationStrategy.valueOf("SEQUENTIAL").getStrategy(), instanceOf(SequentialStrategy.class));
   assertThat(EvaluationStrategy.valueOf("SEQUENTIAL_BATCHED"), is(EvaluationStrategy.SEQUENTIAL_BATCHED));
     assertThat(EvaluationStrategy.valueOf("SEQUENTIAL_BATCHED").getStrategy(), instanceOf(BatchedStrategy.class));
 }
}
