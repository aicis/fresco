package dk.alexandra.fresco.framework.sce.evaluator;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestEvaluationStrategy {

 @Test
 public void testEnums(){
   Assert.assertThat(EvaluationStrategy.valueOf("SEQUENTIAL"), Is.is(EvaluationStrategy.SEQUENTIAL));
   Assert.assertThat(EvaluationStrategy.valueOf("SEQUENTIAL_BATCHED"), Is.is(EvaluationStrategy.SEQUENTIAL_BATCHED));
 }
}
