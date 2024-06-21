package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.tools.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import java.security.MessageDigest;

public class MascotMockSupplier {

  /**
   * Creates a mock resource pool with valid parameters to use for exception testing.
   *
   * @return mock mascot resource pool
   */
  public MascotResourcePool getResourcePool() {
    return new MascotResourcePool() {

      @Override
      public Drbg getRandomGenerator() {
        return null;
      }

      @Override
      public int getNoOfParties() {
        return 0;
      }

      @Override
      public int getMyId() {
        return 0;
      }

      @Override
      public int getInstanceId() {
        return 0;
      }

      @Override
      public FieldDefinition getFieldDefinition() {
        return new BigIntegerFieldDefinition("251");
      }

      @Override
      public MessageDigest getMessageDigest() {
        return null;
      }

      @Override
      public StrictBitVectorSerializer getStrictBitVectorSerializer() {
        return null;
      }

      @Override
      public int getPrgSeedLength() {
        return 0;
      }

      @Override
      public int getNumCandidatesPerTriple() {
        return 0;
      }

      @Override
      public int getModBitLength() {
        return 0;
      }

      @Override
      public FieldElementPrg getLocalSampler() {
        return null;
      }

      @Override
      public int getLambdaSecurityParam() {
        return 0;
      }

      @Override
      public ByteSerializer<HashBasedCommitment> getCommitmentSerializer() {
        return null;
      }

      @Override
      public RotBatch createRot(int otherId, Network network) {
        return null;
      }
    };
  }

  /**
   * Creates mock network for testing.
   *
   * @return mock network
   */
  public Network getNetwork() {
    return new Network() {

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

}
