package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestFakeTripGen {

  private static final BigInteger modulus = new BigInteger(
      "6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
  private static final BigIntegerI alpha =
      BigInt.fromConstant(new BigInteger(
          "5081587041441179438932635098620319894716368628029284292880408086703438041331200877980213770035569812296677935118715454650749402237663859711459266577679205"));
  private BigIntegerI zero = BigInt.fromConstant(BigInteger.ZERO);

  @Test
  public void testTripleGen() {
    int amount = 100000;
    int noOfParties = 2;
    List<SpdzTriple[]> triples = FakeTripGen.generateTriples(amount, noOfParties, modulus, alpha);
    for (SpdzTriple[] t : triples) {
      BigIntegerI a = add(t[0].getA().getShare(), t[1].getA().getShare());
      BigIntegerI b = add(t[0].getB().getShare(), t[1].getB().getShare());
      BigIntegerI c = add(t[0].getC().getShare(), t[1].getC().getShare());

      BigIntegerI shareA = add(t[0].getA().getMac(), t[1].getA().getMac());
      BigIntegerI shareB = add(t[0].getB().getMac(), t[1].getB().getMac());
      BigIntegerI shareC = add(t[0].getC().getMac(), t[1].getC().getMac());

      BigIntegerI actual = a.copy();
      actual.multiply(b);
      actual.mod(modulus);
      Assert.assertEquals(c, actual);

      BigIntegerI zero = BigInt.fromConstant(BigInteger.ZERO);

      Assert.assertEquals(zero, subtract(a, shareA));
      Assert.assertEquals(zero, subtract(b, shareB));
      Assert.assertEquals(zero, subtract(c, shareC));
    }
  }

  private BigIntegerI subtract(BigIntegerI a, BigIntegerI shareA) {
    BigIntegerI copy = shareA.copy();
    BigIntegerI bigIntegerI = a.copy();
    bigIntegerI.multiply(alpha);
    copy.subtract(bigIntegerI);
    copy.mod(modulus);
    return copy;
  }

  private BigIntegerI add(BigIntegerI first, BigIntegerI second) {
    BigIntegerI value = first.copy();
    value.add(second);
    value.mod(modulus);
    return value;
  }

  @Test
  public void testInputMasks() {
    int amount = 1000;
    int noOfParties = 2;
    List<List<SpdzInputMask[]>> inps =
        FakeTripGen.generateInputMasks(amount, noOfParties, modulus, alpha);
    for (int towardsPlayer = 1; towardsPlayer < noOfParties + 1; towardsPlayer++) {
      List<SpdzInputMask[]> inputMasks = inps.get(towardsPlayer - 1);
      for (SpdzInputMask[] masks : inputMasks) {
        SpdzInputMask realMask = masks[towardsPlayer - 1];
        Assert.assertNotNull(realMask.getRealValue());

        SpdzInputMask m1 = masks[0];
        SpdzInputMask m2 = masks[1];
        BigIntegerI share = add(m1.getMask().getShare(), m2.getMask().getShare());
        Assert.assertEquals(realMask.getRealValue(), share);
        BigIntegerI mac = add(m1.getMask().getMac(), m2.getMask().getMac());
        Assert.assertEquals(zero, subtract(share, mac));
      }
    }
  }

  @Test
  public void testInputMasksTowards() {
    int amount = 1000;
    int noOfParties = 2;
    int towardsPlayer = 1;
    List<SpdzInputMask[]> inputMasks =
        FakeTripGen.generateInputMasks(amount, towardsPlayer, noOfParties, modulus, alpha);
    for (SpdzInputMask[] masks : inputMasks) {
      SpdzInputMask realMask = masks[towardsPlayer - 1];
      Assert.assertNotNull(realMask.getRealValue());

      SpdzInputMask m1 = masks[0];
      SpdzInputMask m2 = masks[1];
      BigIntegerI share = add(m1.getMask().getShare(), m2.getMask().getShare());
      Assert.assertEquals(realMask.getRealValue(), share);
      BigIntegerI mac = add(m1.getMask().getMac(), m2.getMask().getMac());
      Assert.assertEquals(zero, subtract(share, mac));
    }
  }

  @Test
  public void testExpPipe() {
    int amount = 2;
    int noOfParties = 2;
    List<SpdzSInt[][]> expPipes = FakeTripGen.generateExpPipes(amount, noOfParties, modulus, alpha);
    for (SpdzSInt[][] pipe : expPipes) {
      SpdzSInt[] as = pipe[0];
      SpdzSInt[] bs = pipe[1];
      BigIntegerI r = add(as[1].getShare(), bs[1].getShare());
      System.out.println(r);
      Assert.assertEquals(r.modInverse(modulus),
          add(as[0].getShare(), bs[0].getShare()));
      BigIntegerI prevR = r;
      for (int i = 0; i < as.length; i++) {
        BigIntegerI share = add(as[i].getShare(), bs[i].getShare());
        BigIntegerI mac = add(as[i].getMac(), bs[i].getMac());
        Assert.assertEquals(zero, subtract(share, mac));
        if (i > 1) {
          BigIntegerI copy = r.copy();
          copy.multiply(prevR);
          Assert.assertEquals(copy, share);
          prevR = share;
        }
      }
    }
  }

  @Test
  public void testBitGen() {
    int amount = 100000;
    int noOfParties = 2;
    List<SpdzSInt[]> bits = FakeTripGen.generateBits(amount, noOfParties, modulus, alpha);
    for (SpdzSInt[] b : bits) {
      BigIntegerI val = add(b[0].getShare(), b[1].getShare());
      BigIntegerI mac = add(b[0].getMac(), b[1].getMac());

      Assert.assertTrue(
          val.asBigInteger().equals(BigInteger.ZERO)
              || val.asBigInteger().equals(BigInteger.ONE));
      Assert.assertEquals(zero, subtract(val, mac));
    }
  }

  @Test
  public void testElementToBytes() {
    SpdzSInt element = new SpdzSInt(
        new BigInt(200), new BigInt(1),
        BigInteger.ZERO);
    ByteBuffer buf = FakeTripGen.elementToBytes(element, 1);
    byte[] arr = buf.array();
    Assert.assertArrayEquals(new byte[]{(byte) 200, 1}, arr);

    try {
      FakeTripGen.elementToBytes(element, 0);
      Assert.fail("Should have cast an exception");
    } catch (RuntimeException e) {

    }

    element = new SpdzSInt(new BigInt(1), new BigInt(200), BigInteger.ZERO);
    buf = FakeTripGen.elementToBytes(element, 1);
    arr = buf.array();
    Assert.assertArrayEquals(new byte[]{1, (byte) 200}, arr);
    try {
      FakeTripGen.elementToBytes(element, 0);
      Assert.fail("Should have cast an exception");
    } catch (RuntimeException e) {

    }
  }

  @Test
  public void testBigIntToBytes() {
    BigIntegerI b = new BigInt(200);
    int size = 1;
    ByteBuffer buf = FakeTripGen.bigIntToBytes(b, size);
    byte[] arr = buf.array();
    Assert.assertArrayEquals(new byte[]{(byte) 200}, arr);
    try {
      FakeTripGen.bigIntToBytes(b, 0);
      Assert.fail("Should have cast an exception");
    } catch (RuntimeException e) {

    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMainFailNoArgs() throws IOException {
    FakeTripGen.main(new String[]{""});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMainFailArgs() throws IOException {
    FakeTripGen.main(new String[]{"-y=true"});
  }

  @Test
  public void testMainMissingArgs() throws IOException {
    FakeTripGen.main(new String[]{"-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=."});
    FakeTripGen.main(new String[]{"-m=187263", "-i=10", "-b=100", "-e=1", "-p=2", "-d=."});
    FakeTripGen.main(new String[]{"-m=187263", "-t=10", "-b=100", "-e=1", "-p=2", "-d=."});
    FakeTripGen.main(new String[]{"-m=187263", "-t=10", "-i=10", "-e=1", "-p=2", "-d=."});
    FakeTripGen.main(new String[]{"-m=187263", "-t=10", "-i=10", "-b=100", "-p=2", "-d=."});
    FakeTripGen.main(new String[]{"-m=187263", "-t=10", "-i=10", "-b=100", "-e=1", "-d=."});
    FakeTripGen.main(new String[]{"-m=187263", "-t=10", "-i=10", "-b=100", "-e=1", "-p=2"});
  }

  @Test
  public void testMain() throws IOException {
    FakeTripGen
        .main(new String[]{"-m=187263", "-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=."});
    try {
      FakeTripGen.cleanup();
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testMainSpecialMod() throws Exception {
    //This test is for a valid modulus where the byte representation contains all 0 in the first byte. 
    FakeTripGen.main(
        new String[]{"-m=131", "-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=.", "-r=true"});
    try {
      FakeTripGen.cleanup();
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testMainInsecureRandom() throws IOException {
    FakeTripGen.main(
        new String[]{"-m=187263", "-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=.", "-r=true"});
    try {
      FakeTripGen.cleanup();
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }
}
