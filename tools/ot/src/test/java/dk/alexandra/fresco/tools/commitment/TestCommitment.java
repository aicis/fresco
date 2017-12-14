package dk.alexandra.fresco.tools.commitment;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.tools.helper.Constants;

public class TestCommitment {
  Commitment comm;
  Drbg rand;

  @Before
  public void setup() {
    rand = new AesCtrDrbg(Constants.seedOne);
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

  @Test
  public void testSerialization() {
    byte[] msg1 = new byte[] { 0x42 };
    comm.commit(rand, msg1);
    CommitmentSerializer serializer = new CommitmentSerializer();
    byte[] serializedComm = serializer.serialize(comm);
    Commitment deserializedComm = serializer.deserialize(serializedComm);
    assertEquals(comm, deserializedComm);
  }

  @Test
  public void testListSerialization() {
    byte[] msg1 = new byte[] { 0x42 };
    Commitment comm1 = new Commitment();
    comm1.commit(rand, msg1);
    byte[] msg2 = new byte[] { 0x56 };
    Commitment comm2 = new Commitment();
    comm2.commit(rand, msg2);
    List<Commitment> list = new ArrayList<>(2);
    list.add(comm1);
    list.add(comm2);
    CommitmentSerializer serializer = new CommitmentSerializer();
    byte[] serializedList = serializer.serialize(list);
    List<Commitment> deserializedList = serializer
        .deserializeList(serializedList);
    assertEquals(list, deserializedList);
  }

  @Test
  public void testEmptyListSerialization() {
    List<Commitment> list = new ArrayList<>(0);
    CommitmentSerializer serializer = new CommitmentSerializer();
    byte[] serializedList = serializer.serialize(list);
    List<Commitment> deserializedList = serializer
        .deserializeList(serializedList);
    assertEquals(list, deserializedList);
  }

  /**** NEGATIVE TESTS. ****/
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
      // The opening must be 32 bytes plus the message length
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
        "6534687420653468742065346874205565346874206534687420653468742055");
    comm.commit(rand, bigInt.toByteArray());
    boolean thrown = false;
    try {
      // The message itself is not enough to open the commitment
      comm.open(new byte[32 + bigInt.toByteArray().length]);
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

  @Test
  public void testNotEqual() {
    comm.commit(rand, new byte[] { 0x42 });
    assertFalse(comm.equals(new Commitment()));
  }

  @Test
  public void testWrongSerialization() {
    boolean thrown = false;
    try {
      CommitmentSerializer serializer = new CommitmentSerializer();
      // This is an illegal length of list of serialized commitments, since each
      // commitment is 32 byte
      serializer.deserialize(new byte[33]);
    } catch (IllegalArgumentException e) {
      assertEquals("The length of the byte array to deserialize is wrong.",
          e.getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }

  @Test
  public void testWrongSerializationList() {
    boolean thrown = false;
    try {
      CommitmentSerializer serializer = new CommitmentSerializer();
      // This is an illegal length of list of serialized commitments, since each
      // commitment is 32 byte
      serializer.deserializeList(new byte[63]);
    } catch (IllegalArgumentException e) {
      assertEquals("The length of the byte array to deserialize is wrong.", e.getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }

  // @Test
  // public void testNoSuchAlgorithm() throws Exception {
  // Field algorithm = Commitment.class.getDeclaredField("hashAlgorithm");
  // Constants.setFinalStatic(algorithm, "sdf", Commitment.class);
  // boolean thrown = false;
  // try {
  // Commitment newCom = new Commitment();
  // } catch (IllegalArgumentException e) {
  // assertEquals("The internally used hash function does not exist",
  // e.getMessage());
  // thrown = true;
  // }
  // assertTrue(thrown);
  // }

}
