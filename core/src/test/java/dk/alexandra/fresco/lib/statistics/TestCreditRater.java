package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestCreditRater {

  private BigInteger modulus = BigInteger.TEN;

  @Test
  public void testConsistency() {
    List<DRes<SInt>> values = new ArrayList<>();
    List<List<DRes<SInt>>> intervals = new ArrayList<>();
    List<List<DRes<SInt>>> scores = new ArrayList<>();

    values.add(new DummyArithmeticSInt(BigInt.fromConstant(BigInteger.ONE, modulus)));
    intervals.add(new ArrayList<>());
    scores.add(new ArrayList<>());

    try {
      new CreditRater(values, intervals, scores);
    } catch (IllegalArgumentException e) {
      Assert.fail("Consistent data should be accepted");
    }

    intervals.add(new ArrayList<>());

    try {
      new CreditRater(values, intervals, scores);
      Assert.fail("Inconsistent data should not be accepted");
    } catch (IllegalArgumentException e) {
      Assert.assertThat(e.getMessage(), Is.is("Inconsistent data"));
    }

    values.add(new DummyArithmeticSInt(new BigInt(1, modulus)));
    try {
      new CreditRater(values, intervals, scores);
      Assert.fail("Inconsistent data should not be accepted");
    } catch (IllegalArgumentException e) {
      Assert.assertThat(e.getMessage(), Is.is("Inconsistent data"));
    }
  }
}
