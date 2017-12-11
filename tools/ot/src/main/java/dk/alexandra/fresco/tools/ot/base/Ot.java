package dk.alexandra.fresco.tools.ot.base;

import java.io.Serializable;

public interface Ot<T extends Serializable> {

  public void send(T messageZero, T messageOne);

  public T receive(Boolean choiceBit);
}
