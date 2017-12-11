package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.util.StrictBitVector;

public interface Ot {

  public void send(StrictBitVector messageZero, StrictBitVector messageOne);

  public StrictBitVector receive(Boolean choiceBit);
}
