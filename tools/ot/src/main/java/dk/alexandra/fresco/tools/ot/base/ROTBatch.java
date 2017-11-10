package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.util.Pair;

import java.math.BigInteger;
import java.util.List;

public interface ROTBatch<T> {

  public List<Pair<T, T>> send(int numMessages);
  public List<T> receive(BigInteger choiceBits, int numBits);
  
}
