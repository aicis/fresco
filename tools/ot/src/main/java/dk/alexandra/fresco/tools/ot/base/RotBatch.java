package dk.alexandra.fresco.tools.ot.base;

import java.util.List;

import dk.alexandra.fresco.framework.util.BitVector;
import dk.alexandra.fresco.framework.util.Pair;

public interface RotBatch<T> {

  public List<Pair<T, T>> send(int numMessages);
  public List<T> receive(BitVector choiceBits, int numBits);
  
}
