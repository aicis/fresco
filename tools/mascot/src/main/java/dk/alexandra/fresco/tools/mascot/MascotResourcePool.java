package dk.alexandra.fresco.tools.mascot;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import java.security.MessageDigest;
import java.util.List;

public interface MascotResourcePool extends NumericResourcePool {

  /**
   * Gets the party ids.
   *
   * @return party ids
   */
  List<Integer> getPartyIds();

  /**
   * Returns the instance ID which is unique for this particular resource pool object, but only in the
   * given execution.
   * 
   * @return the instance ID of this particular object
   *
   */
  int getInstanceId();

  /**
   * Gets bit length of modulus (k security param in Mascot paper).
   *
   * @return modulus bit length
   */
  int getModBitLength();

  /**
   * Gets OT security parameter num bits (lambda in Mascot paper).
   *
   * @return lambda security parameter
   */
  int getLambdaSecurityParam();

  /**
   * Gets number of factors that go into sacrifice step. <br>
   * For each triple we generate, we will generate and numLeftFactors - 1 triples for a single right
   * factor and sacrifice these to authenticate the triple.
   *
   * @return number of factors
   */
  int getNumCandidatesPerTriple();

  /**
   * Gets bit length of seed used to underlying prg.
   *
   * @return prg seed bit length
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
  FieldElementSerializer getFieldElementSerializer();

  /**
   * Gets serializer for {@link StrictBitVector}.
   *
   * @return serializer
   */
  StrictBitVectorSerializer getStrictBitVectorSerializer();

  /**
   * Gets serializer for {@link HashBasedCommitment}.
   *
   * @return serializer
   */
  ByteSerializer<HashBasedCommitment> getCommitmentSerializer();

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
}
