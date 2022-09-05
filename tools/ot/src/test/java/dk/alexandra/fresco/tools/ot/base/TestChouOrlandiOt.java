package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.util.*;
import dk.alexandra.fresco.logging.BatchEvaluationLoggingDecorator;
import dk.alexandra.fresco.tools.helper.HelperForTests;
import dk.alexandra.fresco.tools.helper.RuntimeForTests;
import dk.alexandra.fresco.tools.ot.otextension.CheatingNetworkDecorator;
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
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;

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
            BigInteger bigIntegerResult = new BigInteger(result.toByteArray());
            if (bigIntegerResult.compareTo(staticSpec.getP()) != -1 || bigIntegerResult.compareTo(BigInteger.ZERO) <= 0) {
                fail("Element not in field");
            }

            // check against a precomputed value to ensure we get the same result.
            // This will of course fail if the seed used in the tests changes.
            byte[] expected = Base64.getDecoder().decode("AIJ8xVvqAadbaMfJFX1nfO+gBbTbLgI/q753LfhdL27irzRJAuE35d4J5dJabYv7/LKUk6BfapWe6ONFxMVOW1OZ+b/hPb9OR1nAYXlaSLonJx2okZzUx7eTQqIZzMLM8PY7wcyZCEAhiCTXV9D6z/uFQIZlWBZmhSC8mgRrpTeY9SpRrBOvkz4gAGLynHoqYCHrCni5B4Le+1IklldN9sCNv6qZXNUebdSLvzw1L+TpI4YFQh4wGcQ9SCq8r63lN7xIG2RqoPK/NzgvdZWDbT2j3XUpVl804WwLbR7lsqeagXw+ugevXpUDn45w7hIBUw0C1tOEPlVUHEMT4iI72Fk=");
            if (!Arrays.equals(bigIntegerResult.toByteArray(), expected)) {
                fail("Computed result not matching expected value");
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
            BouncyCastleECCElement test = result.groupOp(eccElement);

            // check against a precomputed value to ensure we get the same result.
            // This will of course fail if the seed used in the tests changes.
            byte[] expected = Base64.getDecoder().decode("BEtK5/CGv5T+TXVXfzKmhmcYrkC/7tb1zkNV6ayQB8qlNIdy125CUqmawW8hW0l1/BBPSpxEanxezSnGPrZ0AqE=");
            if (!Arrays.equals(test.toByteArray(), expected)) {
                fail("Computed result not matching expected value");
            }
        }
        else fail("Not recognized Chou-Orlandi class");
    }

    /**** NEGATIVE TESTS. ****/

    @Test
    public void testUnequalLengthMessages() throws SecurityException, IllegalArgumentException {
        RuntimeForTests testRuntime = new RuntimeForTests();
        boolean choice = true;
        Callable<List<StrictBitVector>> partyOneInit = () -> otSendCheat();
        Callable<List<StrictBitVector>> partyTwoInit = () -> otReceiveCheat(choice);
        List<List<StrictBitVector>> results =
                testRuntime.runPerPartyTasks(Arrays.asList(partyOneInit, partyTwoInit));
        MaliciousException exception = (MaliciousException) results.get(1);
        assertEquals("The length of the two choice messages is not equal", exception.getMessage());
    }

    private List<StrictBitVector> otSendCheat() throws Exception {
        Network network =
                new CheatingNetworkDecorator(
                        new SocketNetwork(RuntimeForTests.defaultNetworkConfiguration(1, Arrays.asList(1, 2))));
        Drbg rand = new AesCtrDrbg(HelperForTests.seedOne);
        Class clazz = this.testClass;
        Constructor[] constructors = clazz.getConstructors();
        Ot otSender = (Ot) constructors[0]
                .newInstance(2, rand, network);
        StrictBitVector msgZero = new StrictBitVector(1024, rand);
        StrictBitVector msgOne = new StrictBitVector(1024, rand);
        otSender.send(msgZero, msgOne);
        List<StrictBitVector> messages = new ArrayList<>(2);
        messages.add(msgZero);
        messages.add(msgOne);
        ((Closeable) network).close();
        return messages;
    }

    private List<StrictBitVector> otReceiveCheat(boolean choice) throws Exception {
        Network network =
                new CheatingNetworkDecorator(
                        new SocketNetwork(RuntimeForTests.defaultNetworkConfiguration(2, Arrays.asList(1, 2))));
            Drbg rand = new AesCtrDrbg(HelperForTests.seedTwo);
            Class clazz = this.testClass;
            Constructor[] constructors = clazz.getConstructors();
            Ot otReceiver = (Ot) constructors[0]
                    .newInstance(1, rand, network);
            ((CheatingNetworkDecorator) network).cheatInReceive(2, new byte[]{0x42, 0x42, 0x42});
            StrictBitVector message = otReceiver.receive(choice);
            List<StrictBitVector> messageList = new ArrayList<>(1);
            messageList.add(message);
            ((Closeable) network).close();
            return messageList;
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

