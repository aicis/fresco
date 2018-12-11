package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinitionBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.builder.numeric.FieldElementBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.framework.network.serializers.BigIntegerWithFixedLengthSerializer;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class TestSpdzDatatypes {

  private ModulusBigInteger modulus = new ModulusBigInteger(100);
  private SpdzSInt elm0 = new SpdzSInt(get(BigInteger.ZERO), get(BigInteger.ZERO));
  private SpdzSInt elm1 = new SpdzSInt(get(BigInteger.ONE), get(BigInteger.ONE));
  private SpdzSInt elm2 = new SpdzSInt(get(BigInteger.ONE), get(BigInteger.ONE));
  private SpdzSInt elmDiff1 =
      new SpdzSInt(get(BigInteger.ZERO), get(BigInteger.ONE));

  @Test
  public void testElementEquals() {
    Assert.assertEquals(elm1, elm1);
    Assert.assertEquals(elm1, elm2);
    Assert.assertNotEquals(elm0, elm2);
    Assert.assertNotEquals(elm0.hashCode(), elm2.hashCode());
    Assert.assertEquals(
        "spdz(FieldElementBigInteger{value=1, modulus=ModulusBigInteger{value=100}}, FieldElementBigInteger{value=1, modulus=ModulusBigInteger{value=100}})",
        elm1.toString());
    SpdzSInt elm3 = new SpdzSInt(get(BigInteger.TEN), get(BigInteger.TEN));
    Assert.assertNotEquals(elm2, elm3);
    Assert.assertNotEquals(elm2, new SpdzSInt(get(BigInteger.ONE), get(BigInteger.ZERO)));
    Assert.assertNotEquals(elm2, "");
    Assert.assertNotEquals(elm2, null);

    SpdzSInt shareNull1 = new SpdzSInt(null, get(BigInteger.ONE));
    SpdzSInt shareNull2 = new SpdzSInt(null, get(BigInteger.ONE));
    Assert.assertEquals(shareNull1, shareNull2);
    Assert.assertNotEquals(shareNull1, elm1);
  }

  private FieldElement get(BigInteger ten) {
    return new FieldElementBigInteger(ten, modulus);
  }

  @Test
  public void testTripleEquals() {
    SpdzTriple trip_empty = new SpdzTriple();
    SpdzTriple trip1 = new SpdzTriple(elm1, elm1, elm1);
    SpdzTriple trip2 = new SpdzTriple(elm1, elm1, elm1);
    SpdzTriple trip3 = new SpdzTriple(elm0, elm1, elm1);
    SpdzTriple tripANull = new SpdzTriple(null, elm1, elm1);
    SpdzTriple tripBNull = new SpdzTriple(elm1, null, elm1);
    SpdzTriple tripCNull = new SpdzTriple(elm1, elm1, null);
    Assert.assertEquals(trip1, trip2);
    Assert.assertNotEquals(trip1, trip3);
    Assert.assertNotEquals(trip1, null);
    Assert.assertNotEquals(trip1, "");
    Assert.assertNotEquals(trip1, trip_empty);
    Assert.assertNotEquals(tripANull, trip1);
    Assert.assertNotEquals(tripBNull, trip1);
    Assert.assertNotEquals(tripCNull, trip1);
    Assert.assertNotEquals(trip1.hashCode(), tripBNull.hashCode());
    Assert.assertNotEquals(tripANull.hashCode(), tripCNull.hashCode());
    Assert
        .assertEquals(
            "SpdzTriple [elementA=spdz(FieldElementBigInteger{value=1, modulus=ModulusBigInteger{value=100}}, FieldElementBigInteger{value=1, modulus=ModulusBigInteger{value=100}}), elementB=spdz(FieldElementBigInteger{value=1, modulus=ModulusBigInteger{value=100}}, FieldElementBigInteger{value=1, modulus=ModulusBigInteger{value=100}}), elementC=spdz(FieldElementBigInteger{value=1, modulus=ModulusBigInteger{value=100}}, FieldElementBigInteger{value=1, modulus=ModulusBigInteger{value=100}})]",
            trip1.toString());
  }

  @Test
  public void testSpdzSIntEquals() {
    SpdzSInt i1 = elm1;
    SpdzSInt i2 = elm2;
    SpdzSInt i3 = elmDiff1;
    Assert.assertEquals(i1, i1);
    Assert.assertEquals(i1, i2);
    Assert.assertNotEquals(i1, null);
    Assert.assertNotEquals(i1, "");
    Assert.assertNotEquals(i1, i3);
    Assert.assertEquals(
        "spdz(FieldElementBigInteger{value=1, modulus=ModulusBigInteger{value=100}}, FieldElementBigInteger{value=1, modulus=ModulusBigInteger{value=100}})",
        i1.toString());
  }

  @Test
  public void testInputMaskEquals() {
    SpdzInputMask mask = new SpdzInputMask(null);
    Assert.assertEquals("SpdzInputMask [mask=null, realValue=null]", mask.toString());
  }

  @Test
  public void testCommitment() throws NoSuchAlgorithmException {
    SpdzCommitment comm = new SpdzCommitment(null, null, new Random(1),
        modulus.getBigInteger().bitLength());
    Assert.assertEquals("SpdzCommitment[v:null, r:null, commitment:null]", comm.toString());
    MessageDigest H = MessageDigest.getInstance("SHA-256");
    SpdzCommitment c = new SpdzCommitment(H, get(BigInteger.ONE), new Random(0),
        modulus.getBigInteger().bitLength());
    BigIntegerWithFixedLengthSerializer serializer =
        new BigIntegerWithFixedLengthSerializer(20, new FieldDefinitionBigInteger(modulus));
    FieldElement c1 = c.computeCommitment(serializer);
    FieldElement c2 = c.computeCommitment(serializer);
    Assert.assertEquals(c1, c2);
  }
}
