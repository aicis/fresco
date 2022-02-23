package dk.alexandra.fresco.tools.bitTriples.prg;

import dk.alexandra.fresco.framework.util.StrictBitVector;

public interface BytePrg {

  /**
   * Deterministically generates random field element.
   *
   * @return random field element
   */
  StrictBitVector getNext(int size);

}
