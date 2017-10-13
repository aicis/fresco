package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SBool;

/**
 * Interface for the basic operations which any binary protocol suite needs to implement.
 *
 */
public interface Binary extends ComputationDirectory {

  /**
   * Creates a secret value from a public value. This is mostly a helper method for making types
   * match - it should not be used to load actual secret values. Use input for this.
   * 
   * @param known The public value to transform.
   * @return A secret representation of the known value.
   */
  DRes<SBool> known(boolean known);

  /**
   * Inputs a secret value to be used in FRESCO protocols. If your party ID is different from the
   * inputter id, your input will be disregarded.
   * 
   * @param in The value to input, or any value if you are not the inputting party.
   * @param inputter The party which gives input.
   * @return A deferred result computing a secret share of the given input.
   */
  DRes<SBool> input(boolean in, int inputter);

  /**
   * Produces a random bit.
   * @return A deferred result computing a secret share of a random bit.
   */
  DRes<SBool> randomBit();

  /**
   * Opens (aka. reveals) the given SBool to all parties.
   * 
   * @param toOpen The SInt to open.
   * @return A deferred result computing the value that the SInt represented.
   */
  DRes<Boolean> open(DRes<SBool> toOpen);

  /**
   * Opens (aka. reveals) the given SBool to only the party with the given Id.
   * 
   * @param toOpen The SInt to open.
   * @param towardsPartyId The Id of the party who should receive the output.
   * @return A deferred result computing the value that the SInt represented.
   */
  DRes<Boolean> open(DRes<SBool> toOpen, int towardsPartyId);

  /**
   * Basic AND operation
   * 
   * @param left The left AND argument
   * @param right The right AND argument
   * @return A deferred result computing <code>left AND right</code>.
   */
  DRes<SBool> and(DRes<SBool> left, DRes<SBool> right);

  /**
   * XOR basic operation. Returns the wire resulting from this operation.
   * 
   * @param left Left XOR argument
   * @param right right XOR argument
   * @return A deferred result computing <code>left XOR right</code>.
   */
  DRes<SBool> xor(DRes<SBool> left, DRes<SBool> right);

  /**
   * Basic NOT operation
   * 
   * @param in The input to be inverted.
   * @return A deferred result computing <code>!in</code>.
   */
  DRes<SBool> not(DRes<SBool> in);
}
