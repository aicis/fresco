package dk.alexandra.fresco.tools.mascot.commit;

import java.util.Arrays;

import org.junit.Test;

import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotMockSupplier;

public class TestCommitmentBasedProtocol {

  private final MascotMockSupplier mockSupplier = new MascotMockSupplier();

  @Test(expected = IllegalArgumentException.class)
  public void testOpenOnDifferentSizes() {
    CommitmentBasedProtocol<StrictBitVector> protocol = new CommitmentBasedProtocol<>(
        mockSupplier.getResourcePool(), mockSupplier.getNetwork(), new StrictBitVectorSerializer());
    protocol.open(Arrays.asList(null, null), Arrays.asList(new byte[] {}));
  }

}
