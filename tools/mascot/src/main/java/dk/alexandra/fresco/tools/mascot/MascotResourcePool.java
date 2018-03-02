package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import java.security.MessageDigest;

public interface MascotResourcePool extends NumericResourcePool {

  /**
   * Returns the instance ID which is unique for this particular resource pool object, but only in
   * the given execution.
   *
   * @return the instance ID of this particular object
   */
  int getInstanceId();

  /**
   * {@link MascotSecurityParameters#getModBitLength()}.
   */
  int getModBitLength();

  /**
   * {@link MascotSecurityParameters#getLambdaSecurityParam()}.
   */
  int getLambdaSecurityParam();

  /**
   * {@link MascotSecurityParameters#getNumCandidatesPerTriple()}.
   */
  int getNumCandidatesPerTriple();

  /**
   * {@link MascotSecurityParameters#getPrgSeedLength()}.
   */
  int getPrgSeedLength();

  /**
   * Gets PRG for generating random field elements locally (for this party only).
   *
   * @return PRG
   */
  FieldElementPrg getLocalSampler();

  /**
   * Gets serializer for {@link FieldElement}.
   *
   * @return serializer
   */
  default ByteSerializer<FieldElement> getFieldElementSerializer() {
    return new FieldElementSerializer(getModulus());
  }

  /**
   * Gets serializer for {@link StrictBitVector}.
   *
   * @return serializer
   */
  default ByteSerializer<StrictBitVector> getStrictBitVectorSerializer() {
    return new StrictBitVectorSerializer();
  }

  /**
   * Gets serializer for {@link HashBasedCommitment}.
   *
   * @return serializer
   */
  default ByteSerializer<HashBasedCommitment> getCommitmentSerializer() {
    return new HashBasedCommitmentSerializer();
  }

  /**
   * Gets the message digest for this protocol suite invocation.
   *
   * @return the message digest
   */
  MessageDigest getMessageDigest();

  /**
   * Creates random oblivious transfer protocol to be used.
   *
   * @param otherId other party that participates in protocol
   * @param network network
   * @return instance of random oblivious transfer protocol
   */
  RotBatch createRot(int otherId, Network network);

  /**
   * The DRBG is useful for protocols which needs a form of shared randomness where the random bytes
   * are not easily guessed by an adversary. This generator will provide exactly that. For explicit
   * security guarantees, we refer to implementations of
   * {@link dk.alexandra.fresco.framework.util.Drbg}.
   *
   * @return An instance of a DRBG.
   */
  Drbg getRandomGenerator();

}
