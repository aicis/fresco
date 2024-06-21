package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.util.*;
import dk.alexandra.fresco.tools.helper.HelperForTests;
import dk.alexandra.fresco.tools.helper.RuntimeForTests;
import dk.alexandra.fresco.tools.ot.otextension.CheatingNetworkDecorator;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.crypto.spec.DHParameterSpec;
import java.io.Closeable;
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
                {ECChouOrlandi.class},
                {BigIntChouOrlandi.class}
        });
    }


    private Class testClass;

    public TestChouOrlandiOt(Class testClass) {
        this.testClass = testClass;
    }

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
        new DrngImpl(randBit);
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

}

