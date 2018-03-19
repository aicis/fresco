package dk.alexandra.fresco.tools.ot.base;

import static org.junit.Assert.assertEquals;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.DHParameterSpec;
import org.junit.Test;

public class TestDhParameters {

  @Test
  public void testGetStaticDhParams()
      throws InvalidParameterSpecException, NoSuchAlgorithmException {
    AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
    SecureRandom commonRand = SecureRandom.getInstance("SHA1PRNG");
    commonRand.setSeed(new byte[] { 0x42 });
    paramGen.init(2048, commonRand);
    AlgorithmParameters params = paramGen.generateParameters();
    assertEquals(params.getParameterSpec(DHParameterSpec.class).getG(),
        DhParameters.getStaticDhParams().getG());
    assertEquals(params.getParameterSpec(DHParameterSpec.class).getP(),
        DhParameters.getStaticDhParams().getP());
  }

}
