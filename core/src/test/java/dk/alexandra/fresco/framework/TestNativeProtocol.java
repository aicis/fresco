package dk.alexandra.fresco.framework;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus;

public class TestNativeProtocol {

  @Test
  public void testEvaluationStatus() {
    Assert.assertThat(EvaluationStatus.HAS_MORE_ROUNDS.toString(), Is.is("HAS_MORE_ROUNDS"));
    Assert.assertThat(EvaluationStatus.IS_DONE.toString(), Is.is("IS_DONE"));
    Assert.assertThat(EvaluationStatus.valueOf("IS_DONE"), Is.is(EvaluationStatus.IS_DONE));
    Assert.assertThat(EvaluationStatus.values()[0], Is.is(EvaluationStatus.IS_DONE));
    Assert.assertThat(EvaluationStatus.values()[1], Is.is(EvaluationStatus.HAS_MORE_ROUNDS));
  }

  
}
