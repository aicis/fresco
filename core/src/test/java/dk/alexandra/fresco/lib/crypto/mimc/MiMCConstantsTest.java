package dk.alexandra.fresco.lib.crypto.mimc;

import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import org.hamcrest.core.Is;
import org.junit.Test;

public class MiMCConstantsTest {

  @Test
  public void rejectionSampling() {
    BigInteger constant = new MimcConstants().getConstant(4, BigInteger.ONE.shiftLeft(127));
    assertThat(constant, Is.is(new BigInteger("21050421667816228993181298429140717896")));
  }
}