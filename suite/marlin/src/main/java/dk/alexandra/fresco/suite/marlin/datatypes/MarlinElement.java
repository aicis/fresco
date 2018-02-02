package dk.alexandra.fresco.suite.marlin.datatypes;

public class MarlinElement<T extends BigUInt<T>> {

  private final T share;
  private final T macShare;

  public MarlinElement(T share, T macShare) {
    this.share = share;
    this.macShare = macShare;
  }



}
