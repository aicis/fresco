package dk.alexandra.fresco.framework.util;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public class SIntPair extends Pair<DRes<SInt>, DRes<SInt>> {

  public SIntPair(DRes<SInt> first,
      DRes<SInt> second) {
    super(first, second);
  }

}
