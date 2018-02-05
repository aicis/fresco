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

  /**
   * Adds constant (open) value to this and returns result. <p>All parties compute their mac share
   * of the public value and add it to the mac share of the authenticated value, however only party
   * 1 adds the public value to is value share.</p>
   *
   * @param other constant, open value
   * @param partyId party ID used to ensure that only one party adds value to share
   * @param macKeyShare mac key share for maccing open value
   * @return result of sum
   */
  public MarlinElement<T> add(T other, int partyId, T macKeyShare, T zeroElement) {
    T otherMac = other.multiply(macKeyShare);
    // only party 1 actually adds value to its share
    T value = (partyId == 1) ? other : zeroElement;
    MarlinElement<T> wrapped = new MarlinElement<>(value, otherMac);
    return add(wrapped);
  }

  public T getShare() {
    return share;
  }

  public T getMacShare() {
    return macShare;
  }

}
