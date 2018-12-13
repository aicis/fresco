package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerModulus;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestStatistics {

  private BigIntegerModulus modulus = new BigIntegerModulus(123123123);
  private BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(modulus);

  @Test(expected = IllegalArgumentException.class)
  public void testCovarianceBadLength() {
    List<DRes<SInt>> input1 = new ArrayList<>();
    input1.add(createSInt(2));
    input1.add(createSInt(2));
    List<DRes<SInt>> input2 = new ArrayList<>();
    input2.add(createSInt(2));
    input2.add(createSInt(2));
    input2.add(createSInt(2));
    new Covariance(input1, input2);

    Assert.fail("Should not be reachable.");
  }

  private DummyArithmeticSInt createSInt(int value) {
    return new DummyArithmeticSInt(definition.createElement(value));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCovarianceMatrixBadLength() {
    List<DRes<SInt>> input1 = new ArrayList<>();
    input1.add(createSInt(2));
    input1.add(createSInt(2));
    List<DRes<SInt>> input2 = new ArrayList<>();
    input2.add(createSInt(2));
    input2.add(createSInt(2));
    input2.add(createSInt(2));
    new CovarianceMatrix(Arrays.asList(input1, input2));
    Assert.fail("Should not be reachable.");
  }
}
