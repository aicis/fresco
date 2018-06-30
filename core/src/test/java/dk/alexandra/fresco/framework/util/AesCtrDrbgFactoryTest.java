package dk.alexandra.fresco.framework.util;

import java.security.NoSuchAlgorithmException;
import org.junit.Test;

public class AesCtrDrbgFactoryTest {

  @Test
  public void testFromDerivedSeed() throws NoSuchAlgorithmException {
    AesCtrDrbgFactory.fromDerivedSeed((byte)0x01, (byte)0x02);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromRandomSeedShortSeed() {
    AesCtrDrbgFactory.fromRandomSeed(new byte[] { 0x42 } );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromRandomSeedLongSeed() {
    byte[] bytes = new byte[33];
    AesCtrDrbgFactory.fromRandomSeed(bytes);
  }

  @Test
  public void testFromSampledSeed() {
    AesCtrDrbgFactory.fromSampledSeed();
  }

}
