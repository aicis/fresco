package dk.alexandra.fresco.suite.spdz.datatypes;

import static org.hamcrest.MatcherAssert.assertThat;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import java.math.BigInteger;
import org.hamcrest.core.StringContains;
import org.junit.Test;

public class TestSpdzSInt {

  private BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(
      ModulusFinder.findSuitableModulus(8));

  @Test
  public void string() {
    FieldElement share = get(BigInteger.ONE);
    FieldElement mac = get(BigInteger.ZERO);
    String result = new SpdzSInt(share, mac).toString();
    assertThat(result, StringContains.containsString(share.toString()));
    assertThat(result, StringContains.containsString(mac.toString()));
  }

  private FieldElement get(BigInteger bigInteger) {
    return definition.createElement(bigInteger);
  }
}
