package dk.alexandra.fresco.tools.ot.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.DHParameterSpec;
import org.junit.Test;

public class TestDhParameters {

  DHParameterSpec staticSpec = DhParameters.getStaticDhParams();

  @Test
  public void validateParameters() {
    // check that p = 2q + 1 is a safe prime, i.e. q is prime too.
    BigInteger subgroupSize = staticSpec.getP().subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
    assertEquals(BigInteger.ZERO,
            staticSpec.getP().subtract(BigInteger.ONE).remainder(BigInteger.valueOf(2)));
    assertTrue(staticSpec.getP().isProbablePrime(80));
    assertTrue(subgroupSize.isProbablePrime(80));
  }

  @Test
  public void validateGenerator() {
    BigInteger p = staticSpec.getP();
    BigInteger q = staticSpec.getP().subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
    BigInteger g = staticSpec.getG();
    assertEquals(BigInteger.ONE, g.modPow(q, p));
  }
}
