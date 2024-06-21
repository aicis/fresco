package dk.alexandra.fresco.tools.ot.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.tools.helper.HelperForTests;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import javax.crypto.spec.DHParameterSpec;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestNaorPinkasOt {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { BigIntNaorPinkas.class }, {ECNaorPinkasOt.class}
    });
  }

  private AbstractNaorPinkasOT ot;
  private Method encryptMessage;
  private Method decryptMessage;
  private Drng randNum;
  private DHParameterSpec staticSpec;


  private Class testClass;

  public TestNaorPinkasOt(Class testClass) {
    this.testClass = testClass;
  }

  /**
   * Construct a NaorPinkasOt instance based on some static Diffie-Hellman parameters.
   *
   * @throws SecurityException Thrown if it is not possible to change private method visibility
   * @throws NoSuchMethodException Thrown if it is not possible to change private method visibility
   */
  @Before
  public void setup()
      throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, InstantiationException {
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

      @Override
      public boolean isAlive() {
        return false;
      }
    };
    staticSpec = DhParameters.getStaticDhParams();
    Class clazz = this.testClass;
    Constructor[] constructors = clazz.getConstructors();
    this.ot = (AbstractNaorPinkasOT) constructors[0]
        .newInstance(2, randBit, network);
    // Change visibility of private methods so they can be tested
    this.encryptMessage = getMethodFromAbstractClass("encryptRandomMessage");
    this.encryptMessage.setAccessible(true);
    this.decryptMessage = getMethodFromAbstractClass("decryptRandomMessage");
    this.decryptMessage.setAccessible(true);
  }

  /**** POSITIVE TESTS. ****/
  @SuppressWarnings("unchecked")
  @Test
  public void testEncDec()
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    BigInteger privateKey = randNum.nextBigInteger(staticSpec.getP());
    InterfaceOtElement publicKey = ot.getGenerator().exponentiation(privateKey);
    Pair<InterfaceNaorPinkasElement, byte[]> encryptionData =
        (Pair<InterfaceNaorPinkasElement, byte[]>) encryptMessage.invoke(ot, publicKey);
    byte[] message = encryptionData.getSecond();
    // Sanity check that the byte array gets initialized, i.e. is not the 0-array
    assertFalse(Arrays.equals(new byte[32], message));
    byte[] decryptedMessage =
        (byte[]) decryptMessage.invoke(ot, encryptionData.getFirst(), privateKey);
    assertArrayEquals(message, decryptedMessage);
  }

  @Test
  public void testNextRandomElement() {
    ECNaorPinkasOt ot = new ECNaorPinkasOt(2, new AesCtrDrbg(HelperForTests.seedOne), null);
    ECCurve curve = CustomNamedCurves.getByName("curve25519").getCurve();
    ECFieldElement element = curve.fromBigInteger(BigInteger.ONE);
    ECFieldElement newElement = ot.nextFieldElement(element);
    assertNotEquals(element, newElement);
    // Except with probability 2^-40 this should be true
    assertTrue(newElement.bitLength() > curve.getFieldSize()-40);
    ECFieldElement nextElement = ot.nextFieldElement(newElement);
    assertTrue(nextElement.bitLength() > curve.getFieldSize()-40);
    assertNotEquals(newElement, nextElement);
  }

  /**** NEGATIVE TESTS. ****/
  @SuppressWarnings("unchecked")
  @Test
  public void testFailedEncDec()
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    BigInteger privateKey = randNum.nextBigInteger(staticSpec.getP());
    InterfaceOtElement publicKey = ot.getGenerator().exponentiation(privateKey);
    Pair<InterfaceNaorPinkasElement, byte[]> encryptionData =
        (Pair<InterfaceNaorPinkasElement, byte[]>) encryptMessage.invoke(ot, publicKey);
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
    Method method = getMethodFromAbstractClass("recoverTrueMessage");
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


  private Method getMethodFromAbstractClass(String methodToSearch) {
    Class<?> clazz = this.testClass;
    while (clazz != null) {
      Method[] methods = clazz.getDeclaredMethods();
      for (Method method : methods) {
        // Test any other things about it beyond the name...
        if (method.getName().equals(methodToSearch)) {
          return method;
        }
      }
      clazz = clazz.getSuperclass();
    }
    return null;
  }
}
