package dk.alexandra.fresco.tools.bitTriples;

import static org.mockito.Mockito.mock;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import org.junit.Test;

public class BitTripleResourcePoolImplTest {

    @Test(expected = IllegalArgumentException.class)
    public void createCote() {
        BitTripleResourcePoolImpl impl = new BitTripleResourcePoolImpl(1,2,3,mock(Drbg.class), mock(BitTripleSecurityParameters.class));
        impl.createCote(1,mock(Network.class),new StrictBitVector(8));
    }
}
