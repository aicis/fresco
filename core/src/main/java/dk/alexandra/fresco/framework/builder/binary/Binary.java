package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * Interface for the basic operations which any binary protocol suite needs to implement.
 * 
 * @author Kasper Damgaard
 *
 */
public interface Binary extends ComputationDirectory {

  DRes<SBool> known(boolean known);

  DRes<SBool> input(boolean in, int inputter);

  DRes<SBool> randomBit();

  /**
   * Opens (aka. reveals) the given SBool to all parties.
   * 
   * @param toOpen The SInt to open.
   * @return The value that the SInt represented.
   */
  DRes<Boolean> open(DRes<SBool> toOpen);

  /**
   * Opens (aka. reveals) the given SBool to only the party with the given Id.
   * 
   * @param toOpen The SInt to open.
   * @param towardsPartyId The Id of the party who should receive the output.
   * @return The value that the SInt represented.
   */
  DRes<Boolean> open(DRes<SBool> toOpen, int towardsPartyId);

  /**
   * Basic AND operation
   * 
   * @param left The left AND argument
   * @param right The right AND argument
   * @return An outgoing wire where the result is stored.
   */
  DRes<SBool> and(DRes<SBool> left, DRes<SBool> right);

  /**
   * XOR basic operation. Returns the wire resulting from this operation.
   * 
   * @param left Left XOR argument
   * @param right right XOR argument
   * @return A wire where the result is stored.
   */
  DRes<SBool> xor(DRes<SBool> left, DRes<SBool> right);

  /**
   * Basic NOT operation
   * 
   * @param in The input to be inverted.
   * @return An outgoing wire where the result is stored.
   */
  DRes<SBool> not(DRes<SBool> in);
}
