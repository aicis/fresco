package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.util.Pair;

public interface Rot<T> {

  public Pair<T, T> send() throws MaliciousOtException, FailedOtException;;

  public T receive(Boolean choiceBit)
      throws MaliciousOtException, FailedOtException;;
  
}
