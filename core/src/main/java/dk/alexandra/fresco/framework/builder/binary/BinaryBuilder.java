package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.List;

/**
 * Interface for the basic operations which any binary protocol suite needs to implement.
 * 
 * @author Kasper Damgaard
 *
 */
public interface BinaryBuilder {

  Computation<SBool> known(boolean known);

  List<Computation<SBool>> known(boolean[] known);

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

  /**
   * Appends a copy protocol to the current protocol copying the value of one computation to an
   * other.
   *
   * @param src the source computation
   * @return a computation holding the copy of the source
   */
  Computation<SBool> copy(Computation<SBool> src);


}
