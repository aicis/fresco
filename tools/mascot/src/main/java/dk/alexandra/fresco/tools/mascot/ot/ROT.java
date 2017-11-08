package dk.alexandra.fresco.tools.mascot.ot;

import dk.alexandra.fresco.framework.util.Pair;

public interface ROT<T> {

  public Pair<T, T> send();
  public T receive(Boolean choiceBit);
  
}
