package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for the basic operations which any binary protocol suite needs to implement.
 * 
 * @author Kasper Damgaard
 *
 */
public interface BinaryBuilder {

  Computation<SBool> known(boolean known);

  default List<Computation<SBool>> known(boolean[] known) {
    List<Computation<SBool>> res = new ArrayList<>();
    for (int i = 0; i < known.length; i++) {
      res.add(known(known[i]));
    }
    return res;
  }

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

  /**
   * Basic AND operation
   * 
   * @param left The left AND argument
   * @param right The right AND argument
   * @return An outgoing wire where the result is stored.
   */
  Computation<SBool> and(Computation<SBool> left, Computation<SBool> right);

  /**
   * XOR basic operation. Returns the wire resulting from this operation.
   * 
   * @param left Left XOR argument
   * @param right right XOR argument
   * @return A wire where the result is stored.
   */
  Computation<SBool> xor(Computation<SBool> left, Computation<SBool> right);

  /**
   * Basic NOT operation
   * 
   * @param in The input to be inverted.
   * @return An outgoing wire where the result is stored.
   */
  Computation<SBool> not(Computation<SBool> in);
}
