package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * Interface for the basic operations which any binary protocol suite needs to implement.
 * 
 * @author Kasper Damgaard
 *
 */
public interface BinaryBuilder {

  Computation<SBool> known(boolean known);

  Computation<SBool> input(boolean in, int inputter);

  Computation<SBool> randomBit();
  
  /**
   * Opens (aka. reveals) the given SBool to all parties.
   * 
   * @param toOpen The SInt to open.
   * @return The value that the SInt represented.
   */
  Computation<Boolean> open(Computation<SBool> toOpen);

  /**
   * Opens (aka. reveals) the given SBool to only the party with the given Id.
   * 
   * @param toOpen The SInt to open.
   * @param towardsPartyId The Id of the party who should receive the output.
   * @return The value that the SInt represented.
   */
  Computation<Boolean> open(Computation<SBool> toOpen, int towardsPartyId);


  Computation<SBool> and(Computation<SBool> left, Computation<SBool> right);

  Computation<SBool> and(Computation<SBool> left, boolean right);
  
  Computation<SBool> xor(Computation<SBool> left, Computation<SBool> right);

  Computation<SBool> xor(Computation<SBool> left, boolean right);

  Computation<SBool> not(Computation<SBool> in);
  
}
