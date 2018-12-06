package dk.alexandra.fresco.tools.mascot.elgen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import dk.alexandra.fresco.framework.builder.numeric.Modulus;
import dk.alexandra.fresco.framework.util.SecretSharer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.field.MascotFieldElement;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.List;
import org.junit.Test;

public class TestShareUtils {

  private final Modulus modulus = new Modulus("251");
  private final FieldElementPrg sampler = new FieldElementPrgImpl(new StrictBitVector(256));
  private final SecretSharer<MascotFieldElement> shareUtils = new AdditiveSecretSharer(sampler, modulus);
  
  @Test
  public void testAdditiveShare() {
    MascotFieldElement input = new MascotFieldElement(BigInteger.ONE, modulus);
    List<MascotFieldElement> shares = shareUtils.share(input, 2);
    assertEquals(2, shares.size());
    assertNotEquals(shares.get(0), shares.get(1));
  }
  
  @Test
  public void testRecombineReversesShare() {
    MascotFieldElement input = new MascotFieldElement(BigInteger.ONE, modulus);
    List<MascotFieldElement> shares = shareUtils.share(input, 3);
    MascotFieldElement actual = shareUtils.recombine(shares);
    CustomAsserts.assertEquals(input, actual);
  }
  
}
