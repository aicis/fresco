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

  /**
   * Basic AND operation
   * 
   * @param left The left AND argument
   * @param right The right AND argument
   * @return An outgoing wire where the result is stored.
   */
  Computation<SBool> and(Computation<SBool> left, Computation<SBool> right);

  /**
   * Basic AND operation, but lets the application programmer choose the outgoing wire.
   * 
   * @param left The left AND argument.
   * @param right The right AND argument.
   * @param out The outgoing wire where the result is stored.
   */
  void and(Computation<SBool> left, Computation<SBool> right, Computation<SBool> out);

  Computation<SBool> and(Computation<SBool> left, boolean right);

  /**
   * XOR basic operation. Returns the wire resulting from this operation.
   * 
   * @param left Left XOR argument
   * @param right right XOR argument
   * @return A wire where the result is stored.
   */
  Computation<SBool> xor(Computation<SBool> left, Computation<SBool> right);

  /**
   * XOR basic operation, but lets the application programmer choose the outgoing wire instead of
   * creating a new one.
   * 
   * @param leftInWireXor Left XOR argument
   * @param rightInWireXor right XOR argument
   * @param outWireXor output wire where the result is stored.
   */
  void xor(Computation<SBool> leftInWireXor, Computation<SBool> rightInWireXor,
      Computation<SBool> outWireXor);

  Computation<SBool> xor(Computation<SBool> left, boolean right);

  /**
   * Basic NOT operation
   * 
   * @param in The input to be inverted.
   * @return An outgoing wire where the result is stored.
   */
  Computation<SBool> not(Computation<SBool> in);

  /**
   * Basic NOT operation, but lets the application programmer choose the outgoing wire.
   * 
   * @param in The input to be inverted
   * @param out The outgoing wire where the result is stored.
   */
  void not(Computation<SBool> in, Computation<SBool> out);

  /**
   * Appends a copy protocol to the current protocol copying the value of one computation to an
   * other.
   *
   * @param src the source computation
   * @return a computation holding the copy of the source
   */
  Computation<SBool> copy(Computation<SBool> src);



}
