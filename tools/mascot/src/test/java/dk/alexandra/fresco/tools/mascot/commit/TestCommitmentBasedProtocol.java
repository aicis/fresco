package dk.alexandra.fresco.tools.mascot.commit;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotMockSupplier;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import java.util.Arrays;

import org.junit.Test;


public class TestCommitmentBasedProtocol {

  private final MascotMockSupplier mockSupplier = new MascotMockSupplier();

  @Test(expected = IllegalArgumentException.class)
  public void testOpenOnDifferentSizes() {
    CommitmentBasedInput<StrictBitVector> protocol = new MockCommitmentBasedInput(
        mockSupplier.getResourcePool(), mockSupplier.getNetwork(), new StrictBitVectorSerializer());
    protocol.open(Arrays.asList(null, null), Arrays.asList(new byte[] {}));
  }

  private class MockCommitmentBasedInput extends CommitmentBasedInput<StrictBitVector> {
    MockCommitmentBasedInput(MascotResourcePool resourcePool,
        Network network,
        ByteSerializer<StrictBitVector> serializer) {
      super(resourcePool, network, serializer);
    }
  }

}
