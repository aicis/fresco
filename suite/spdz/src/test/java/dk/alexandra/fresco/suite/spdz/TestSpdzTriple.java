package dk.alexandra.fresco.suite.spdz;

import static org.hamcrest.MatcherAssert.assertThat;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.math.BigInteger;
import org.hamcrest.core.StringContains;
import org.junit.Test;

public class TestSpdzTriple {

  private BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(
      ModulusFinder.findSuitableModulus(8));

  @Test
  public void create() {
    new SpdzTriple();
  }

  @Test
  public void string() {
    SpdzSInt a = new SpdzSInt(get(BigInteger.ONE), get(BigInteger.ZERO));
    SpdzSInt b = new SpdzSInt(get(BigInteger.ZERO), get(BigInteger.ZERO));
    String result = new SpdzTriple(a, b, a).toString();
    assertThat(result, StringContains.containsString(a.toString()));
    assertThat(result, StringContains.containsString(b.toString()));
  }

  private FieldElement get(BigInteger bigInteger) {
    return definition.createElement(bigInteger);
  }
}
