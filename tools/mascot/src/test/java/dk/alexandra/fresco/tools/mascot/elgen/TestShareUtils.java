package dk.alexandra.fresco.tools.mascot.elgen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.CustomAsserts;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrgImpl;
import java.math.BigInteger;
import java.util.List;
import org.junit.Test;

public class TestShareUtils {

  private final BigInteger modulus = new BigInteger("251");
  private final int modBitLength = 8;
  private final FieldElementPrg sampler = new FieldElementPrgImpl(new StrictBitVector(256));
  private final Sharer shareUtils = new AdditiveSharer(sampler, modulus, modBitLength);
  
  @Test
  public void testAdditiveShare() {
    FieldElement input = new FieldElement(BigInteger.ONE, modulus, modBitLength);
    List<FieldElement> shares = shareUtils.share(input, 2);
    assertEquals(2, shares.size());
    assertNotEquals(shares.get(0), shares.get(1));
  }
  
  @Test
  public void testRecombineReversesShare() {
    FieldElement input = new FieldElement(BigInteger.ONE, modulus, modBitLength);
    List<FieldElement> shares = shareUtils.share(input, 3);
    FieldElement actual = shareUtils.recombine(shares);
    CustomAsserts.assertEquals(input, actual);
  }
  
}