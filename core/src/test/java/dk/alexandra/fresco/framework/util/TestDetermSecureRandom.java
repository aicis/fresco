package dk.alexandra.fresco.framework.util;

import java.util.BitSet;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Test;

import ch.qos.logback.core.net.SyslogOutputStream;
import dk.alexandra.fresco.framework.MPCException;

public class TestDetermSecureRandom {

  @Test
  public void testBasics() {
    DetermSecureRandom r1 = new DetermSecureRandom();
    DetermSecureRandom r2 = new DetermSecureRandom();
    r1.setSeed("seed1".getBytes());
    r2.setSeed("seed2".getBytes());
    
    byte[] bytesFrom1 = new byte[128];
    byte[] bytesFrom2 = new byte[128];
    
    r1.nextBytes(bytesFrom1);
    r2.nextBytes(bytesFrom2);
    Assert.assertThat(bytesFrom1, IsNot.not(bytesFrom2));
    
    r2.setSeed("seed1".getBytes());
    
    r2.nextBytes(bytesFrom2);
    Assert.assertThat(bytesFrom1, Is.is(bytesFrom2));
  }

  
  @Test
  public void testLimitedAmount() {
    DetermSecureRandom r1 = new DetermSecureRandom(10);
    
    r1.setSeed("seed1".getBytes());
    
    byte[] bytes = new byte[30];
    r1.nextBytes(bytes);

    for(int i =10; i< 20; i++) {
      Assert.assertThat(bytes[i], Is.is(bytes[i+10]));
    }
  }

  @Test(expected=MPCException.class)
  public void testTooBigAmount() {
    new DetermSecureRandom(500);
    Assert.fail();
  }
  
}

