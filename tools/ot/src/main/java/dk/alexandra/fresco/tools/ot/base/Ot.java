package dk.alexandra.fresco.tools.ot.base;

public interface Ot<T> {

  public void send(T messageZero, T messageOne);
  public T receive(Boolean choiceBit);
  
}
