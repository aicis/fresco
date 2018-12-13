package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerModulus;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.ModulusFinder;
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

  private static final BigIntegerModulus modulus = new BigIntegerModulus(
      "670390396497129854978701249912381411527384857"
          + "747113652742596601302650153670646435425544544324427938945505888949343122395116528647057599"
          + "4074291745908195329");
  private static final BigIntegerFieldDefinition definition = new BigIntegerFieldDefinition(
      modulus);
  private static final FieldElement alpha = definition.createElement(
      "50815870414411794389326350986203198"
          + "947163686280292842928804080867034380413312008779802137700355698122966779351187154546507494"
          + "02237663859711459266577679205");
  private FieldElement zero = definition.createElement(0);

  @Test
  public void testTripleGen() {
    int amount = 100000;
    int noOfParties = 2;
    List<SpdzTriple[]> triples = FakeTripGen
        .generateTriples(amount, noOfParties, definition, alpha);
    for (SpdzTriple[] t : triples) {
      FieldElement a = t[0].getA().getShare().add(t[1].getA().getShare());
      FieldElement b = t[0].getB().getShare().add(t[1].getB().getShare());
      FieldElement c = t[0].getC().getShare().add(t[1].getC().getShare());

      FieldElement shareA = t[0].getA().getMac().add(t[1].getA().getMac());
      FieldElement shareB = t[0].getB().getMac().add(t[1].getB().getMac());
      FieldElement shareC = t[0].getC().getMac().add(t[1].getC().getMac());

      FieldElement actual = a.multiply(b);
      Assert.assertEquals(c, actual);

      FieldElement zero = definition.createElement(0);

      Assert.assertEquals(zero, subtract(a, shareA));
      Assert.assertEquals(zero, subtract(b, shareB));
      Assert.assertEquals(zero, subtract(c, shareC));
    }
  }

  private FieldElement subtract(FieldElement a, FieldElement shareA) {
    FieldElement fieldElement = a.multiply(alpha);
    fieldElement.multiply(alpha);
    FieldElement copy = shareA.subtract(fieldElement);
    copy.subtract(fieldElement);
    return copy;
  }

  @Test
  public void testInputMasks() {
    int amount = 1000;
    int noOfParties = 2;
    List<List<SpdzInputMask[]>> inps =
        FakeTripGen.generateInputMasks(amount, noOfParties, definition, alpha);
    for (int towardsPlayer = 1; towardsPlayer < noOfParties + 1; towardsPlayer++) {
      List<SpdzInputMask[]> inputMasks = inps.get(towardsPlayer - 1);
      for (SpdzInputMask[] masks : inputMasks) {
        SpdzInputMask realMask = masks[towardsPlayer - 1];
        Assert.assertNotNull(realMask.getRealValue());

        SpdzInputMask m1 = masks[0];
        SpdzInputMask m2 = masks[1];
        FieldElement share = m1.getMask().getShare().add(m2.getMask().getShare());
        Assert.assertEquals(realMask.getRealValue(), share);
        FieldElement mac = m1.getMask().getMac().add(m2.getMask().getMac());
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
        FakeTripGen.generateInputMasks(amount, towardsPlayer, noOfParties, definition, alpha);
    for (SpdzInputMask[] masks : inputMasks) {
      SpdzInputMask realMask = masks[towardsPlayer - 1];
      Assert.assertNotNull(realMask.getRealValue());

      SpdzInputMask m1 = masks[0];
      SpdzInputMask m2 = masks[1];
      FieldElement share = m1.getMask().getShare().add(m2.getMask().getShare());
      Assert.assertEquals(realMask.getRealValue(), share);
      FieldElement mac = m1.getMask().getMac().add(m2.getMask().getMac());
      Assert.assertEquals(zero, subtract(share, mac));
    }
  }

  @Test
  public void testExpPipe() {
    int amount = 2;
    int noOfParties = 2;
    List<SpdzSInt[][]> expPipes = FakeTripGen
        .generateExpPipes(amount, noOfParties, definition, alpha);
    for (SpdzSInt[][] pipe : expPipes) {
      SpdzSInt[] as = pipe[0];
      SpdzSInt[] bs = pipe[1];
      FieldElement r = as[1].getShare().add(bs[1].getShare());
      Assert.assertEquals(getBigInteger(r).modInverse(modulus.getBigInteger()),
          getBigInteger(as[0].getShare().add(bs[0].getShare())));
      FieldElement prevR = r;
      for (int i = 0; i < as.length; i++) {
        FieldElement share = as[i].getShare().add(bs[i].getShare());
        FieldElement mac = as[i].getMac().add(bs[i].getMac());
        Assert.assertEquals(zero, subtract(share, mac));
        if (i > 1) {
          FieldElement copy = r.multiply(prevR);
          Assert.assertEquals(copy, share);
          prevR = share;
        }
      }
    }
  }

  private BigInteger getBigInteger(FieldElement r) {
    return definition.convertRepresentation(r).mod(definition.getModulus());
  }

  @Test
  public void testBitGen() {
    int amount = 100000;
    int noOfParties = 2;
    List<SpdzSInt[]> bits = FakeTripGen.generateBits(amount, noOfParties, definition, alpha);
    for (SpdzSInt[] b : bits) {
      FieldElement val = b[0].getShare().add(b[1].getShare());
      FieldElement mac = b[0].getMac().add(b[1].getMac());

      BigInteger bigIntegerValue = getBigInteger(val);
      Assert.assertTrue(
          bigIntegerValue.equals(BigInteger.ZERO) || bigIntegerValue.equals(BigInteger.ONE));
      Assert.assertEquals(zero, subtract(val, mac));
    }
  }

  @Test
  public void testElementToBytes() {
    SpdzSInt element = new SpdzSInt(definition.createElement(200), definition.createElement(1));
    ByteBuffer buf = FakeTripGen.elementToBytes(element, 1);
    byte[] arr = buf.array();
    Assert.assertArrayEquals(new byte[]{(byte) 200, 1}, arr);

    try {
      FakeTripGen.elementToBytes(element, 0);
      Assert.fail("Should have cast an exception");
    } catch (RuntimeException e) {

    }

    element = new SpdzSInt(definition.createElement(1), definition.createElement(200));
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
    FieldElement b = definition.createElement(200);
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

  private String modulusArg = "-m=" + ModulusFinder.findSuitableModulus(8).toString();

  @Test
  public void testMainMissingArgs() throws IOException {
    FakeTripGen.main(new String[]{"-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=."});
    FakeTripGen.main(new String[]{modulusArg, "-i=10", "-b=100", "-e=1", "-p=2", "-d=."});
    FakeTripGen.main(new String[]{modulusArg, "-t=10", "-b=100", "-e=1", "-p=2", "-d=."});
    FakeTripGen.main(new String[]{modulusArg, "-t=10", "-i=10", "-e=1", "-p=2", "-d=."});
    FakeTripGen.main(new String[]{modulusArg, "-t=10", "-i=10", "-b=100", "-p=2", "-d=."});
    FakeTripGen.main(new String[]{modulusArg, "-t=10", "-i=10", "-b=100", "-e=1", "-d=."});
    FakeTripGen.main(new String[]{modulusArg, "-t=10", "-i=10", "-b=100", "-e=1", "-p=2"});
  }

  @Test
  public void testMain() throws IOException {
    FakeTripGen
        .main(new String[]{modulusArg, "-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=."});
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
        new String[]{modulusArg, "-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=.", "-r=true"});
    try {
      FakeTripGen.cleanup();
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testMainInsecureRandom() throws IOException {
    FakeTripGen.main(
        new String[]{modulusArg, "-t=10", "-i=10", "-b=100", "-e=1", "-p=2", "-d=.", "-r=true"});
    try {
      FakeTripGen.cleanup();
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }
}
