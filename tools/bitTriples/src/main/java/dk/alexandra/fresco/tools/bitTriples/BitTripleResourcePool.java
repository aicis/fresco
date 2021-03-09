package dk.alexandra.fresco.tools.bitTriples;

import dk.alexandra.fresco.tools.commitment.HashBasedCommitment;
import dk.alexandra.fresco.tools.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrg;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.CoteFactory;
import java.security.MessageDigest;

public interface BitTripleResourcePool extends NumericResourcePool {

  /**
   * Gets the field definition.
   *
   * @return field defintion
   */
  FieldDefinition getFieldDefinition();

  /**
   * Returns the instance ID which is unique for this particular resource pool object, but only in
   * the given execution.
   *
   * @return the instance ID of this particular object
   */
  int getInstanceId();

  /**
   * {@link FieldDefinition#getBitLength()}.
   */
  int getModBitLength();

  /**
   * {@link BitTripleSecurityParameters#getComputationalSecurityBitParameter()}.
   */
  int getComputationalSecurityBitParameter();

  /**
   * {@link BitTripleSecurityParameters#getStatisticalSecurityByteParameter()}.
   */
  int getStatisticalSecurityByteParameter();

  /**
   * {@link BitTripleSecurityParameters#getPrgSeedBitLength()}.
   */
  int getPrgSeedBitLength();

  /**
   * Gets PRG for generating random field elements locally (for this party only).
   *
   * @return PRG
   */
  BytePrg getLocalSampler();

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

  CoteFactory createCote(int otherId, Network network, StrictBitVector choices);

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
