package dk.alexandra.fresco.tools.commitment;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
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
    comm = new Commitment(128);
  }

  /**** POSITIVE TESTS. ****/
  @Test
  public void testHonestExecution() {
    byte[] msg = { (byte) 0x12, (byte) 0x42 };
    Serializable openInfo = comm.commit(rand, msg);
    byte[] res = (byte[]) comm.open(openInfo);
    assertEquals(res, msg);
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
      // Security parameter must be at least 1
      comm = new Commitment(0);
    } catch (IllegalArgumentException e) {
      assertEquals("Illegal constructor parameters", e.getMessage());
      thrown = true;
    }
    assertEquals(thrown, true);
    thrown = false;
    try {
      // Randomness generator must not be null
      Boolean val = true;
      comm = new Commitment(2);
      comm.commit(null, val);
    } catch (NullPointerException e) {
      thrown = true;
    }
    assertEquals(thrown, true);
  }

  @Test
  public void testAlreadyCommitted() {
    String firstMsg = "First!";
    Serializable openInfo = comm.commit(rand, firstMsg);
    String secondMsg = "Me, me, me!";
    boolean thrown = false;
    try {
      comm.commit(rand, secondMsg);
    } catch (IllegalStateException e) {
      assertEquals("Already committed", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
    // Check we can still open correctly
    String res = (String) comm.open(openInfo);
    assertEquals(firstMsg, res);
  }

  @Test
  public void testNoCommitmentMade() {
    boolean thrown = false;
    String firstMsg = "First!";
    try {
      comm.open(firstMsg);
    } catch (IllegalStateException e) {
      assertEquals("No commitment to open", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }

  @Test
  public void testBadOpening() {
    BigInteger bigInt = new BigInteger("15646534687420546");
    comm.commit(rand, bigInt);
    boolean thrown = false;
    try {
      // The message itself is not enough to open the commitment
      comm.open(bigInt);
    } catch (MaliciousException e) {
      assertEquals(
          "The object given to the open method is not a proper commitment opening object.",
          e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
    thrown = false;
    try {
      // Try to open using the opening info of another commitment
      Commitment comm2 = new Commitment(128);
      BigInteger bigInt2 = new BigInteger("424242424242424242");
      Serializable openInfo2 = comm2.commit(rand, bigInt2);
      comm.open(openInfo2);
    } catch (MaliciousException e) {
      assertEquals(
          "The opening info does not match the commitment.", e.getMessage());
      thrown = true;
    }
    assertEquals(true, thrown);
  }
}
