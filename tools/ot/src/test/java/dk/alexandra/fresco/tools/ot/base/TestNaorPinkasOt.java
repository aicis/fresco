package dk.alexandra.fresco.tools.ot.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.tools.helper.HelperForTests;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import javax.crypto.spec.DHParameterSpec;
import org.junit.Before;
import org.junit.Test;

public class TestNaorPinkasOt {
  private NaorPinkasOt ot;
  private Method encryptMessage;
  private Method decryptMessage;
  private Drng randNum;
  private DHParameterSpec staticSpec;

  /**
   * Construct a NaorPinkasOt instance based on some static Diffie-Hellman parameters.
   *
   * @throws SecurityException Thrown if it is not possible to change private method visibility
   * @throws NoSuchMethodException Thrown if it is not possible to change private method visibility
   */
  @Before
  public void setup() throws NoSuchMethodException, SecurityException {
    Drbg randBit = new AesCtrDrbg(HelperForTests.seedOne);
    randNum = new DrngImpl(randBit);
    // fake network
    Network network = new Network() {
      @Override
      public void send(int partyId, byte[] data) {}

      @Override
      public byte[] receive(int partyId) {
        return null;
      }

      @Override
      public int getNoOfParties() {
        return 0;
      }
    };
    staticSpec = DhParameters.getStaticDhParams();
    this.ot = new NaorPinkasOt(2, randBit, network, staticSpec);
    // Change visibility of private methods so they can be tested
    this.encryptMessage =
        NaorPinkasOt.class.getDeclaredMethod("encryptRandomMessage", BigInteger.class);
    this.encryptMessage.setAccessible(true);
    this.decryptMessage = NaorPinkasOt.class.getDeclaredMethod("decryptRandomMessage",
        BigInteger.class, BigInteger.class);
    this.decryptMessage.setAccessible(true);
  }


  /**** POSITIVE TESTS. ****/
  @SuppressWarnings("unchecked")
  @Test
  public void testEncDec()
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    BigInteger privateKey = randNum.nextBigInteger(staticSpec.getP());
    BigInteger publicKey = staticSpec.getG().modPow(privateKey, staticSpec.getP());
    Pair<BigInteger, byte[]> encryptionData =
        (Pair<BigInteger, byte[]>) encryptMessage.invoke(ot, publicKey);
    byte[] message = encryptionData.getSecond();
    // Sanity check that the byte array gets initialized, i.e. is not the 0-array
    assertFalse(Arrays.equals(new byte[32], message));
    byte[] decryptedMessage =
        (byte[]) decryptMessage.invoke(ot, encryptionData.getFirst(), privateKey);
    assertArrayEquals(message, decryptedMessage);
  }

  /**** NEGATIVE TESTS. ****/
  @SuppressWarnings("unchecked")
  @Test
  public void testFailedEncDec()
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    BigInteger privateKey = randNum.nextBigInteger(staticSpec.getP());
    BigInteger publicKey = staticSpec.getG().modPow(privateKey, staticSpec.getP());
    Pair<BigInteger, byte[]> encryptionData =
        (Pair<BigInteger, byte[]>) encryptMessage.invoke(ot, publicKey);
    byte[] message = encryptionData.getSecond();
    // Sanity check that the byte array gets initialized, i.e. is not the 0-array
    assertEquals(32, message.length);
    assertFalse(Arrays.equals(new byte[32], message));
    message[(32) - 1] ^= 0x01;
    byte[] decryptedMessage =
        (byte[]) decryptMessage.invoke(ot, encryptionData.getFirst(), privateKey);
    assertFalse(Arrays.equals(message, decryptedMessage));
  }

  @Test
  public void testUnequalLengthMessages() throws SecurityException, IllegalArgumentException,
  IllegalAccessException, NoSuchMethodException {
    Method method = ot.getClass().getDeclaredMethod("recoverTrueMessage", byte[].class,
        byte[].class, byte[].class, boolean.class);
    // Remove private
    method.setAccessible(true);
    boolean thrown = false;
    try {
      method.invoke(ot, new byte[] { 0x42, 0x42 }, new byte[] { 0x42 }, new byte[] { 0x42 }, true);
    } catch (InvocationTargetException e) {
      assertEquals("The length of the two choice messages is not equal",
          e.getTargetException().getMessage());
      thrown = true;
    }
    assertTrue(thrown);
  }
}
