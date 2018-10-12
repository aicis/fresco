package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

/**
 * Logical operators on secret arithmetic representations of boolean values. <p>NOTE: all inputs are
 * assumed to represent 0 or 1 values only. The result is undefined if other values are passed
 * in.</p>
 */
public interface Logical extends ComputationDirectory {
  // TODO: this is starting to look a lot like the Binary computation directory...

  /**
   * Computes logical AND of inputs. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SInt> and(DRes<SInt> bitA, DRes<SInt> bitB);

  /**
   * Computes logical OR of inputs. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SInt> or(DRes<SInt> bitA, DRes<SInt> bitB);

  /**
   * Computes logical XOR of inputs. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SInt> xor(DRes<SInt> bitA, DRes<SInt> bitB);

  /**
   * Computes logical XOR of inputs. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SInt> xorKnown(OInt knownBit, DRes<SInt> secretBit);

  /**
   * Computes logical NOT of input. <p>NOTE: Input must represent 0 or 1 values only.</p>
   */
  DRes<SInt> not(DRes<SInt> secretBit);

  /**
   * Computes logical OR of all input bits. <p> NOTE: Inputs must represent 0 or 1 values only.
   * </p>
   */
  DRes<SInt> orOfList(DRes<List<DRes<SInt>>> bits);

  /**
   * Given a list of bits, computes or of each neighbor pair of bits, i.e., given b1, b2, b3, b4,
   * will output b1 OR b2, b3 OR b4. <p>Also handles uneven number of elements.</p>
   */
  DRes<List<DRes<SInt>>> orNeighbors(List<DRes<SInt>> bits);

}
