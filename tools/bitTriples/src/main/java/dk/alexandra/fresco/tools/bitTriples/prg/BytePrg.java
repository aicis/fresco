package dk.alexandra.fresco.tools.bitTriples.prg;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.ArrayList;
import java.util.List;

public interface BytePrg {

  /**
   * Deterministically generates random field element.
   *
   * @return random field element
   */
  StrictBitVector getNext(int size);

}
