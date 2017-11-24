package dk.alexandra.fresco.tools.ot.base;

import java.util.List;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;

public interface RotBatch<T> {

  public List<Pair<T, T>> send(int numMessages);
  public List<T> receive(StrictBitVector choiceBits, int numBits);
  
}