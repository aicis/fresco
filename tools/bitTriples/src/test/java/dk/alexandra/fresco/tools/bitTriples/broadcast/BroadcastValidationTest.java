package dk.alexandra.fresco.tools.bitTriples.broadcast;

import static org.mockito.Mockito.mock;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.bitTriples.BitTripleResourcePool;
import java.util.ArrayList;
import org.junit.Test;

public class BroadcastValidationTest {

    @Test(expected = MaliciousException.class)
    public void validateDigestsThrowsCorrectException() {
        BroadcastValidation broadcastValidation = new BroadcastValidation(mock(BitTripleResourcePool.class),mock(Network.class));
        ArrayList<byte[]> digests = new ArrayList<>();
        digests.add(new byte[]{24});
        broadcastValidation.validateDigests(new byte[]{23},digests);
    }
}
