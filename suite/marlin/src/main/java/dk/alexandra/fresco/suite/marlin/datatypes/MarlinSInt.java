package dk.alexandra.fresco.suite.marlin.datatypes;

import dk.alexandra.fresco.framework.value.SInt;

public class MarlinSInt<T extends BigUInt<T>> implements SInt {

  private final T share;
  private final T macShare;

  public MarlinSInt(T share, T macShare) {
    this.share = share;
    this.macShare = macShare;
  }

  public MarlinSInt<T> add(MarlinSInt<T> other) {
    return new MarlinSInt<>(share.add(other.share), macShare.add(other.macShare));
  }

  public MarlinSInt<T> subtract(MarlinSInt<T> other) {
    return new MarlinSInt<>(share.subtract(other.share), macShare.subtract(other.macShare));
  }

  public MarlinSInt<T> multiply(T other) {
    return new MarlinSInt<>(share.multiply(other), macShare.multiply(other));
  }

  @Override
  public String toString() {
    return "MarlinSInt{" +
        "share=" + share +
        ", macShare=" + macShare +
        '}';
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
  public MarlinSInt<T> addConstant(T other, int partyId, T macKeyShare, T zeroElement) {
    T otherMac = other.multiply(macKeyShare);
    // only party 1 actually adds value to its share
    T value = (partyId == 1) ? other : zeroElement;
    MarlinSInt<T> wrapped = new MarlinSInt<>(value, otherMac);
    return add(wrapped);
  }

  public T getShare() {
    return share;
  }

  public T getMacShare() {
    return macShare;
  }

  @Override
  public SInt out() {
    return this;
  }

}
