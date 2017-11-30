package dk.alexandra.fresco.tools.ot.base;

import java.io.Serializable;

public interface Ot<T extends Serializable> {

  public void send(T messageZero, T messageOne)
      throws MaliciousOtException, FailedOtException;

  public T receive(Boolean choiceBit)
      throws MaliciousOtException, FailedOtException;;
  
}
