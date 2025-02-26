package dk.alexandra.fresco.tools.commitment;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drbg;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

public class TestCommitment {

  HashBasedCommitment comm;
  Drbg rand;
  private static final int myId = 2;

  @Before
  public void setup() {
    rand = AesCtrDrbgFactory.fromDerivedSeed((byte) 0x42);
    comm = new HashBasedCommitment();
  }

  /**** POSITIVE TESTS. ****/
  @Test
  public void testHonestExecution() {
    byte[] msg = {(byte) 0x12, (byte) 0x42};
    byte[] openInfo = comm.commit(myId, rand, msg);
    byte[] res = comm.open(myId, openInfo);
    assertArrayEquals(res, msg);
  }

  @Test
  public void testEmptyMessage() {
    byte[] msg = {};
    byte[] openInfo = comm.commit(myId, rand, msg);
    byte[] res = comm.open(myId, openInfo);
    assertArrayEquals(res, msg);
  }

  @Test
  public void testSerialization() {
    byte[] msg1 = new byte[]{0x42};
    comm.commit(myId, rand, msg1);
    HashBasedCommitmentSerializer serializer = new HashBasedCommitmentSerializer();
    byte[] serializedComm = serializer.serialize(comm);
    HashBasedCommitment deserializedComm = serializer.deserialize(serializedComm);
    assertArrayEquals(comm.getCommitmentValue(), deserializedComm
        .getCommitmentValue());
  }

  @Test
  public void testListSerialization() {
    byte[] msg1 = new byte[]{0x42};
    HashBasedCommitment comm1 = new HashBasedCommitment();
    comm1.commit(myId, rand, msg1);
    byte[] msg2 = new byte[]{0x56};
    HashBasedCommitment comm2 = new HashBasedCommitment();
    comm2.commit(myId, rand, msg2);
    List<HashBasedCommitment> list = new ArrayList<>(2);
    list.add(comm1);
    list.add(comm2);
    HashBasedCommitmentSerializer serializer = new HashBasedCommitmentSerializer();
    byte[] serializedList = serializer.serialize(list);
    List<HashBasedCommitment> deserializedList = serializer
        .deserializeList(serializedList);
    for (int i = 0; i < deserializedList.size(); i++) {
      assertArrayEquals(list.get(i).getCommitmentValue(), deserializedList.get(
          i).getCommitmentValue());
    }
  }

  @Test
  public void testEmptyListSerialization() {
    List<HashBasedCommitment> list = new ArrayList<>(0);
    HashBasedCommitmentSerializer serializer = new HashBasedCommitmentSerializer();
    byte[] serializedList = serializer.serialize(list);
    List<HashBasedCommitment> deserializedList = serializer
        .deserializeList(serializedList);
    assertEquals(list, deserializedList);
  }

  /**** NEGATIVE TESTS. ****/
  @Test
  public void testIllegalInit() {
    HashBasedCommitment comm;
    boolean thrown = false;
    try {
      // Randomness generator must not be null
      byte[] val = new byte[]{0x01};
      comm = new HashBasedCommitment();
      comm.commit(myId, null, val);
    } catch (NullPointerException e) {
      thrown = true;
    }
    assertTrue(thrown);
  }

  @Test
  public void testAlreadyCommitted() {
    String firstMsg = "First!";
    byte[] openInfo = comm.commit(myId, rand, firstMsg.getBytes());
    String secondMsg = "Me, me, me!";
    boolean thrown = false;
    try {
      comm.commit(myId, rand, secondMsg.getBytes());
    } catch (IllegalStateException e) {
      assertEquals("Already committed", e.getMessage());
      thrown = true;
    }
    assertTrue(thrown);
    // Check we can still open correctly
    String res = new String(comm.open(myId, openInfo));
    assertEquals(firstMsg, res);
  }

  @Test
  public void testNoCommitmentMade() {
    boolean thrown = false;
    String firstMsg = "First!";
    try {
      comm.open(myId, firstMsg.getBytes());
    } catch (IllegalStateException e) {
      assertEquals("No commitment to open", e.getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }

  @Test
  public void testTooSmallOpening() {
    BigInteger bigInt = new BigInteger("15646534687420546");
    comm.commit(myId, rand, bigInt.toByteArray());
    boolean thrown = false;
    try {
      // The opening must be 32 bytes plus the message length
      comm.open(myId, bigInt.toByteArray());
    } catch (MaliciousException e) {
      assertEquals("The opening info is too small to be a commitment.",
          e.getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }

  @Test
  public void testBadOpening() {
    BigInteger bigInt = new BigInteger(
        "6534687420653468742065346874205565346874206534687420653468742055");
    comm.commit(myId, rand, bigInt.toByteArray());
    boolean thrown = false;
    try {
      // The message itself is not enough to open the commitment
      comm.open(myId, new byte[4 + 32 + bigInt.toByteArray().length]);
    } catch (MaliciousException e) {
      assertEquals("The party id does not match with the commitment party id.",
          e.getMessage());
      thrown = true;
    }
    assertTrue(thrown);
    thrown = false;
    try {
      // Try to open using the opening info of another commitment
      HashBasedCommitment comm2 = new HashBasedCommitment();
      BigInteger bigInt2 = new BigInteger("424242424242424242");
      byte[] openInfo2 = comm2.commit(myId, rand, bigInt2.toByteArray());
      comm.open(myId, openInfo2);
    } catch (MaliciousException e) {
      assertEquals(
          "The opening info does not match the commitment.", e.getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }

  @Test
  public void testSingleBitDiffBadOpening() {
    Random random = new Random(42);
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    byte[] opening = comm.commit(myId, rand, bytes);
    boolean thrown = false;
    try {
      // flip bit
      opening[9] = (byte) (opening[9] ^ 1);
      comm.open(myId, opening);
    } catch (MaliciousException e) {
      assertEquals("The opening info does not match the commitment.",
          e.getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }

  @Test
  public void testPartyIdDoesNotMatch() {
    Random random = new Random(42);
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    byte[] opening = comm.commit(myId, rand, bytes);
    boolean thrown = false;
    try {
      comm.open(1, opening);
    } catch (MaliciousException e) {
      assertEquals("The party id does not match with the commitment party id.",
          e.getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testNotEqual() {
    comm.commit(myId, rand, new byte[]{0x42});
    assertNotEquals(comm, new HashBasedCommitment());
    assertNotEquals("something", comm);
  }

  @Test
  public void testWrongSerialization() {
    boolean thrown = false;
    try {
      HashBasedCommitmentSerializer serializer = new HashBasedCommitmentSerializer();
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
      HashBasedCommitmentSerializer serializer = new HashBasedCommitmentSerializer();
      // This is an illegal length of list of serialized commitments, since each
      // commitment is 32 byte
      serializer.deserializeList(new byte[63]);
    } catch (IllegalArgumentException e) {
      assertEquals("The length of the byte array to deserialize is wrong.", e.getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }
}
