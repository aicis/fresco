package dk.alexandra.fresco.tools.mascot.elgen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.SecretSharer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.List;
import org.junit.Test;

public class TestShareUtils {

  private final BigInteger modulus = new BigInteger("251");
  private final BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(modulus);
  private final FieldElementPrg sampler = new FieldElementPrgImpl(new StrictBitVector(256),
      definition);
  private final SecretSharer<FieldElement> shareUtils = new AdditiveSecretSharer(sampler);
  
  @Test
  public void testAdditiveShare() {
    FieldElement input = definition.createElement(1);
    List<FieldElement> shares = shareUtils.share(input, 2);
    assertEquals(2, shares.size());
    assertNotEquals(shares.get(0), shares.get(1));
  }
  
  @Test
  public void testRecombineReversesShare() {
    FieldElement input = definition.createElement(1);
    List<FieldElement> shares = shareUtils.share(input, 3);
    FieldElement actual = shareUtils.recombine(shares);
    CustomAsserts.assertEquals(input, actual);
  }
  
}
