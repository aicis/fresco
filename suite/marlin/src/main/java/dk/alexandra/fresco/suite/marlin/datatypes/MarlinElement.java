package dk.alexandra.fresco.suite.marlin.datatypes;

public class MarlinElement<T extends BigUInt<T>> {

  private final T share;
  private final T macShare;

  public MarlinElement(T share, T macShare) {
    this.share = share;
    this.macShare = macShare;
  }

  public MarlinElement<T> add(MarlinElement<T> other) {
    return new MarlinElement<>(share.add(other.share), macShare.add(other.macShare));
  }

  public MarlinElement<T> multiply(T other) {
    return new MarlinElement<>(share.multiply(other), macShare.multiply(other));
  }

  public T getShare() {
    return share;
  }

  public T getMacShare() {
    return macShare;
  }

}
