package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.util.ModulusFinder;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
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

  @Test
  public void testCommitment() throws NoSuchAlgorithmException {
    SpdzCommitment comm = new SpdzCommitment(null, null, new Random(1),
        definition.getModulus().bitLength());
    Assert.assertEquals("SpdzCommitment[v:null, r:null, commitment:null]", comm.toString());
    MessageDigest H = MessageDigest.getInstance("SHA-256");
    SpdzCommitment c = new SpdzCommitment(H, definition.createElement(1), new Random(0),
        definition.getModulus().bitLength());
    byte[] c1 = c.computeCommitment(definition);
    byte[] c2 = c.computeCommitment(definition);
    Assert.assertEquals(c1, c2);
  }
}
