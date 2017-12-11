package dk.alexandra.fresco.tools.commitment;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.MaliciousException;

public class TestCommitment {
  Commitment comm;
  Random rand;

  @Before
  public void setup() {
    rand = new Random(42);
    comm = new Commitment();
  }

  /**** POSITIVE TESTS. ****/
  @Test
  public void testHonestExecution() {
    byte[] msg = { (byte) 0x12, (byte) 0x42 };
    byte[] openInfo = comm.commit(rand, msg);
    byte[] res = comm.open(openInfo);
    assertArrayEquals(res, msg);
  }

  @Test
  public void testEmptyMessage() {
    byte[] msg = {};
    byte[] openInfo = comm.commit(rand, msg);
    byte[] res = comm.open(openInfo);
    assertArrayEquals(res, msg);
  }

  /****
   * NEGATIVE TESTS.
   * 
   * @throws FailedCommitmentException
   ****/
  @Test
  public void testIllegalInit() {
    Commitment comm;
    boolean thrown = false;
    try {
      // Randomness generator must not be null
      byte[] val = new byte[] { 0x01 };
      comm = new Commitment();
      comm.commit(null, val);
    } catch (NullPointerException e) {
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testAlreadyCommitted() {
    String firstMsg = "First!";
    byte[] openInfo = comm.commit(rand, firstMsg.getBytes());
    String secondMsg = "Me, me, me!";
    boolean thrown = false;
    try {
      comm.commit(rand, secondMsg.getBytes());
    } catch (IllegalStateException e) {
      assertEquals("Already committed", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
    // Check we can still open correctly
    String res = new String(comm.open(openInfo));
    assertEquals(firstMsg, res);
  }

  @Test
  public void testNoCommitmentMade() {
    boolean thrown = false;
    String firstMsg = "First!";
    try {
      comm.open(firstMsg.getBytes());
    } catch (IllegalStateException e) {
      assertEquals("No commitment to open", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testTooSmallOpening() {
    BigInteger bigInt = new BigInteger("15646534687420546");
    comm.commit(rand, bigInt.toByteArray());
    boolean thrown = false;
    try {
      // The message itself is not enough to open the commitment
      comm.open(bigInt.toByteArray());
    } catch (MaliciousException e) {
      assertEquals("The opening info is too small to be a commitment.",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testBadOpening() {
    BigInteger bigInt = new BigInteger(
        "1564653468742054656526586400808453435874240857403407808403368744803453123"
            + "1564653468742054656526586400808453435874240857403407808403368744803453123"
            + "1564653468742054656526586400808453435874240857403407808403368744803453123"
            + "1564653468742054656526586400808453435874240857403407808403368744803453123"
            + "1564653468742054656526586400808453435874240857403407808403368744803453123");
    comm.commit(rand, bigInt.toByteArray());
    boolean thrown = false;
    try {
      // The message itself is not enough to open the commitment
      comm.open(bigInt.toByteArray());
    } catch (MaliciousException e) {
      assertEquals("The opening info does not match the commitment.",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
    thrown = false;
    try {
      // Try to open using the opening info of another commitment
      Commitment comm2 = new Commitment();
      BigInteger bigInt2 = new BigInteger("424242424242424242");
      byte[] openInfo2 = comm2.commit(rand, bigInt2.toByteArray());
      comm.open(openInfo2);
    } catch (MaliciousException e) {
      assertEquals(
          "The opening info does not match the commitment.", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }
}
