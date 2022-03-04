package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.*;
import dk.alexandra.fresco.tools.helper.HelperForTests;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.crypto.spec.DHParameterSpec;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestChouOrlandiOt {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {BouncyCastleChouOrlandi.class},
                {BigIntChouOrlandi.class}
        });
    }


    private Class testClass;

    public TestChouOrlandiOt(Class testClass) {
        this.testClass = testClass;
    }

    private AbstractChouOrlandiOT ot;
    private Method hashToFieldElement;
    private Drng randNum;
    private DHParameterSpec staticSpec;
    /**
     * Construct a ChouOrlandiOt instance based on some static Diffie-Hellman parameters.
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
        };
        staticSpec = DhParameters.getStaticDhParams();
        Class clazz = this.testClass;
        Constructor[] constructors = clazz.getConstructors();
        this.ot = (AbstractChouOrlandiOT) constructors[0]
                .newInstance(2, randBit, network);
        // Change visibility of private methods so they can be tested
        this.hashToFieldElement = getMethodFromAbstractClass("hashToFieldElement");
        this.hashToFieldElement.setAccessible(true);
    }


    /**** POSITIVE TESTS. ****/

    @Test
    public void testHashToFieldElement() throws InvocationTargetException, IllegalAccessException {
        if (this.testClass == BigIntChouOrlandi.class) {
            BigInteger randBigInt = randNum.nextBigInteger(staticSpec.getP());
            BigIntElement bigIntElement = new BigIntElement(randBigInt, staticSpec.getP());
            BigIntElement result = (BigIntElement) this.hashToFieldElement.invoke(this.ot, bigIntElement, "Separation|Tag");
            BigInteger BigIntegerResult = new BigInteger(result.toByteArray());
            if (BigIntegerResult.compareTo(staticSpec.getP()) != -1 || BigIntegerResult.compareTo(BigInteger.ZERO) <= 0) {
                fail("Element not in field");
            }
        } else if (this.testClass == BouncyCastleChouOrlandi.class) {
            X9ECParameters ecP = CustomNamedCurves.getByName("curve25519");
            ECCurve curve = ecP.getCurve();
            BigInteger dhModulus = curve.getOrder();
            ECPoint dhGenerator = ecP.getG();
            ECPoint randElement = dhGenerator.multiply(randNum.nextBigInteger(dhModulus));
            BouncyCastleECCElement eccElement = new BouncyCastleECCElement(randElement);
            BouncyCastleECCElement result = (BouncyCastleECCElement) this.hashToFieldElement.invoke(this.ot,eccElement, "Separation|Tag");
            // if we can calculate something without an exception, everything went well
            result.groupOp(eccElement);
        }
        else fail("Not recognized Chou-Orlandi class");
    }

//    @SuppressWarnings("unchecked")
//    @Test
//    public void testEncDec()
//            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        BigInteger privateKey = randNum.nextBigInteger(staticSpec.getP());
//        InterfaceOtElement publicKey = ot.getGenerator().exponentiation(privateKey);
//        Pair<InterfaceOtElement, byte[]> encryptionData =
//                (Pair<InterfaceOtElement, byte[]>) encryptMessage.invoke(ot, publicKey);
//        byte[] message = encryptionData.getSecond();
//        // Sanity check that the byte array gets initialized, i.e. is not the 0-array
//        assertFalse(Arrays.equals(new byte[32], message));
//        byte[] decryptedMessage =
//                (byte[]) decryptMessage.invoke(ot, encryptionData.getFirst(), privateKey);
//        assertArrayEquals(message, decryptedMessage);
//    }


    /**** NEGATIVE TESTS. ****/


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

