package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import org.junit.Assert;
import org.junit.Test;

public class TestSpdzDatatypes {

  private BigIntegerFieldDefinition definition =
      new BigIntegerFieldDefinition(ModulusFinder.findSuitableModulus(8));

  @Test
  public void testInputMaskEquals() {
    SpdzInputMask mask = new SpdzInputMask(null);
    Assert.assertEquals("SpdzInputMask [mask=null, realValue=null]", mask.toString());
  }

}
