package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
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

  SpdzElement elm_empty = new SpdzElement();
  SpdzElement elm1 = new SpdzElement(BigInteger.ONE, BigInteger.ONE, BigInteger.TEN);
  SpdzElement elm2 = new SpdzElement(BigInteger.ONE, BigInteger.ONE, BigInteger.TEN);
  SpdzElement elmDiff1 = new SpdzElement(BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN);
  
  @Test
  public void testElementEquals() {
    Assert.assertEquals(elm1, elm1);    
    Assert.assertEquals(elm1, elm2);
    Assert.assertNotEquals(elm_empty, elm2);
    Assert.assertNotEquals(elm_empty.hashCode(), elm2.hashCode());
    Assert.assertEquals("spdz(1, 1)", elm1.toString());
    byte[] bytes = new byte[2];    
    bytes[0] = BigInteger.ZERO.toByteArray()[0];
    bytes[1] = BigInteger.ONE.toByteArray()[0];
    SpdzElement elm3 = new SpdzElement(bytes, BigInteger.TEN, BigInteger.TEN.toByteArray().length);
    Assert.assertNotEquals(elm2, elm3);
    Assert.assertNotEquals(elm2, new SpdzElement(BigInteger.ONE, BigInteger.ZERO, BigInteger.TEN));
    Assert.assertNotEquals(elm2, "");
    Assert.assertNotEquals(elm2, null);    
    
    SpdzElement modNull1 = new SpdzElement(BigInteger.ONE, BigInteger.ONE, null);
    SpdzElement modNull2 = new SpdzElement(BigInteger.ONE, BigInteger.ONE, null);
    Assert.assertEquals(modNull1, modNull2);
    Assert.assertNotEquals(modNull1, elm1);
    
    SpdzElement modDiff = new SpdzElement(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
    Assert.assertNotEquals(modDiff, elm1);
    
    SpdzElement shareNull1 = new SpdzElement(null, BigInteger.ONE, BigInteger.TEN);
    SpdzElement shareNull2 = new SpdzElement(null, BigInteger.ONE, BigInteger.TEN);
    Assert.assertEquals(shareNull1, shareNull2);
    Assert.assertNotEquals(shareNull1, elm1);
  }
  
  @Test
  public void testTripleEquals() {    
    SpdzTriple trip_empty = new SpdzTriple();
    SpdzTriple trip1 = new SpdzTriple(elm1, elm1, elm1);
    SpdzTriple trip2 = new SpdzTriple(elm1, elm1, elm1);
    SpdzTriple trip3 = new SpdzTriple(elm_empty, elm1, elm1);
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
    Assert.assertEquals("SpdzTriple [a=spdz(1, 1), b=spdz(1, 1), c=spdz(1, 1)]", trip1.toString());    
  }
  
  @Test
  public void testSpdzSIntEquals() {
    SpdzSInt i_empty = new SpdzSInt();
    SpdzSInt i_empty2 = new SpdzSInt();
    SpdzSInt i1 = new SpdzSInt(elm1);
    SpdzSInt i2 = new SpdzSInt(elm2);
    SpdzSInt i3 = new SpdzSInt(elmDiff1);
    Assert.assertEquals(i1, i1);
    Assert.assertEquals(i1, i2);
    Assert.assertEquals(i_empty, i_empty2);
    Assert.assertNotEquals(i_empty, i2);
    Assert.assertNotEquals(i1, null);
    Assert.assertNotEquals(i1, "");
    Assert.assertNotEquals(i1, i3);
    Assert.assertEquals("SpdzSInt(spdz(1, 1))", i1.toString());
  }
  
  @Test
  public void testInputMaskEquals() {
    SpdzInputMask mask = new SpdzInputMask(null);
    Assert.assertEquals("SpdzInputMask [mask=null, realValue=null]",mask.toString());
  }
  
  @Test
  public void testCommitment() throws NoSuchAlgorithmException {
    SpdzCommitment comm = new SpdzCommitment(null, null, null);
    Assert.assertEquals("SpdzCommitment[v:null, r:null, commitment:null]", comm.toString());
    MessageDigest H = MessageDigest.getInstance("SHA-256");
    SpdzCommitment c = new SpdzCommitment(H, BigInteger.ONE, new Random(0));
    BigInteger c1 = c.computeCommitment(BigInteger.TEN);
    BigInteger c2 = c.computeCommitment(BigInteger.TEN);
    Assert.assertEquals(c1, c2);
  }
}
