package dk.alexandra.fresco.tools.mascot;

import java.util.List;

import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.network.serializers.StrictBitVectorSerializer;
import dk.alexandra.fresco.tools.commitment.Commitment;
import dk.alexandra.fresco.tools.commitment.CommitmentSerializer;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;

public interface MascotResourcePool extends NumericResourcePool {

  /**
   * Gets the party ids.
   * 
   * @return party ids
   */
  List<Integer> getPartyIds();

  /**
   * Gets bit length of modulus (k security param in Mascot paper).
   * 
   * @return modulus bit length
   */
  int getModBitLength();

  /**
   * Gets OT security parameter num bits (lambda in Mascot paper)
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
   * Serializer for {@link FieldElement}.
   * 
   * @return serializer
   */
  FieldElementSerializer getFieldElementSerializer();

  /**
   * Serializer for {@link StrictBitVector}
   * 
   * @return serializer
   */
  StrictBitVectorSerializer getStrictBitVectorSerializer();

  /**
   * Serializer for {@link Commitment}
   * 
   * @return serializer
   */
  CommitmentSerializer getCommitmentSerializer();

}
