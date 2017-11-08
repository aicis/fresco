package dk.alexandra.fresco.tools.ot.base;

public interface OT<T> {

  public void send(T messageZero, T messageOne);
  public T receive(Boolean choiceBit);
  
}
