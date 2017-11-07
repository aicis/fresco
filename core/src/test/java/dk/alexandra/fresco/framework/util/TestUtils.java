package dk.alexandra.fresco.framework.util;

import java.util.BitSet;

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
  
  @Test
  public void testFromHex() {
    Assert.assertThat(ByteArithmetic.toBoolean("00"), Is.is(new boolean[]{false, false, false, false, false, false, false, false}));
    Assert.assertThat(ByteArithmetic.toBoolean("FF"), Is.is(new boolean[]{true, true, true, true, true, true, true, true}));
    Assert.assertThat(ByteArithmetic.toBoolean("7F"), Is.is(new boolean[]{false, true, true, true, true, true, true, true}));
    Assert.assertThat(ByteArithmetic.toBoolean("3F"), Is.is(new boolean[]{false, false, true, true, true, true, true, true}));
    Assert.assertThat(ByteArithmetic.toBoolean("1F"), Is.is(new boolean[]{false, false, false, true, true, true, true, true}));
    Assert.assertThat(ByteArithmetic.toBoolean("0F"), Is.is(new boolean[]{false, false, false, false, true, true, true, true}));
    Assert.assertThat(ByteArithmetic.toBoolean("00FF"), Is.is(new boolean[]{false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true,}));
    Assert.assertThat(ByteArithmetic.toBoolean("01FF"), Is.is(new boolean[]{false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true,}));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testBadHex(){
    ByteArithmetic.toBoolean("F");
  }
  
  @Test
  public void testXor() {
    Assert.assertThat(ByteArithmetic.xor((byte)8, (byte)4), Is.is((byte)12));
    Assert.assertThat(ByteArithmetic.xor((byte)0, (byte)4), Is.is((byte)4));
    Assert.assertThat(ByteArithmetic.xor((byte)255, (byte)255), Is.is((byte)0));
    Assert.assertThat(ByteArithmetic.xor((byte)1, (byte)255), Is.is((byte)254));
  }

  @Test
  public void testNot() {
    Assert.assertThat(ByteArithmetic.not((byte)8), Is.is((byte)0));
    Assert.assertThat(ByteArithmetic.not((byte)0), Is.is((byte)1));
    Assert.assertThat(ByteArithmetic.not((byte)256), Is.is((byte)1));
    Assert.assertThat(ByteArithmetic.not((byte)1), Is.is((byte)0));
  }
  
  @Test
  public void testIntToBitSet() {
    Assert.assertThat(ByteArithmetic.intToBitSet(0), Is.is(new BitSet()));
    BitSet one = new BitSet();
    one.set(0);
    Assert.assertThat(ByteArithmetic.intToBitSet(1), Is.is(one));
    BitSet two = new BitSet();
    two.set(1);
    Assert.assertThat(ByteArithmetic.intToBitSet(2), Is.is(two));
    
    BitSet large = new BitSet();
    large.set(0);
    large.set(1);
    large.set(2);
    large.set(3);
    large.set(4);
    large.set(5);
    large.set(6);
    large.set(7);
    Assert.assertThat(ByteArithmetic.intToBitSet(255), Is.is(large));
  }
}

