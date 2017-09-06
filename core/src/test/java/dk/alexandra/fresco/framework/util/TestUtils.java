package dk.alexandra.fresco.framework.util;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestUtils {

  @Test
  public void testBasic() {
    long beforeTime = System.nanoTime();
    Timing timing = new Timing();
    timing.start();
    long whenStartedTime = System.nanoTime();
    try{
      Thread.sleep(200);
    } catch(Exception ignored) {
    }
    long beforeStoppedTime = System.nanoTime();
    long stopped = timing.stop();
    long afterTime = System.nanoTime();
    Assert.assertTrue(timing.getTimeInNanos() <= afterTime-beforeTime);
    Assert.assertTrue(timing.getTimeInNanos() >= beforeStoppedTime-whenStartedTime);
    Assert.assertThat(stopped, Is.is(timing.getTimeInNanos()));
  }

  @Test
  public void testToHex() {
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{false, false, false, false, false, false, false, false}), Is.is("00"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true, true, true, true, true}), Is.is("ff"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true, true, true, true}), Is.is("7f"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true, true, true}), Is.is("3f"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true, true}), Is.is("1f"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true}), Is.is("0f"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{false, true, true, true, true, true, true, true, true,}), Is.is("00ff"));
    Assert.assertThat(ByteArithmetic.toHex(new boolean[]{true, true, true, true, true, true, true, true, true,}), Is.is("01ff"));
  }
  
/*
 * TODO  @Test
 
  public void testFormatting(){
    String format = Timing.formatNanosAsMilliSeconds(200);
    Assert.assertThat(format, Is.is("2.0E-4 ms"));
  }
 */
}

