package dk.alexandra.fresco.tools.mascot.ot;

public interface OT<T> {

  public void send(T messageZero, T messageOne);
  public T receive(Boolean choiceBit);
  
}
