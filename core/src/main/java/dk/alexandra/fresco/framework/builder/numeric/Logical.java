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
   * Computes logical AND of inputs. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SInt> andKnown(DRes<OInt> knownBit, DRes<SInt> secretBit);

  /**
   * Computes logical XOR of inputs. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SInt> xorKnown(DRes<OInt> knownBit, DRes<SInt> secretBit);

  /**
   * Computes logical NOT of input. <p>NOTE: Input must represent 0 or 1 values only.</p>
   */
  DRes<SInt> not(DRes<SInt> secretBit);

  /**
   * Opens secret bits, possibly performing conversion before producing final open value. <p>NOTE:
   * Input must represent 0 or 1 values only.</p>
   */
  DRes<OInt> openAsBit(DRes<SInt> secretBit);

  /**
   * Batch opening of bits.
   */
  DRes<List<DRes<OInt>>> openAsBits(DRes<List<DRes<SInt>>> secretBits);

  /**
   * Computes pairwise logical AND of input bits. <p>NOTE: Inputs must represent 0 or 1 values
   * only.</p>
   */
  DRes<List<DRes<SInt>>> pairWiseAndKnown(DRes<List<DRes<OInt>>> knownBits,
      DRes<List<DRes<SInt>>> secretBits);

  /**
   * Computes pairwise logical AND of input bits. <p>NOTE: Inputs must represent 0 or 1 values
   * only.</p>
   */
  DRes<List<DRes<SInt>>> pairWiseAnd(DRes<List<DRes<SInt>>> bitsA,
      DRes<List<DRes<SInt>>> bitsB);

  /**
   * Computes pairwise logical OR of input bits. <p>NOTE: Inputs must represent 0 or 1 values
   * only.</p>
   */
  DRes<List<DRes<SInt>>> pairWiseOr(DRes<List<DRes<SInt>>> bitsA,
      DRes<List<DRes<SInt>>> bitsB);

  /**
   * Computes pairwise logical XOR of input bits. <p>NOTE: Inputs must represent 0 or 1 values
   * only.</p>
   */
  DRes<List<DRes<SInt>>> pairWiseXorKnown(DRes<List<DRes<OInt>>> knownBits,
      DRes<List<DRes<SInt>>> secretBits);

  /**
   * Computes logical OR of all input bits. <p>NOTE: Inputs must represent 0 or 1 values only.</p>
   */
  DRes<SInt> orOfList(DRes<List<DRes<OInt>>> bits);

}
