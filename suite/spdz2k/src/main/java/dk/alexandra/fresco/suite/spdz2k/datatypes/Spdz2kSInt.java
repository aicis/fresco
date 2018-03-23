package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.value.SInt;

/**
 * Represents an authenticated, secret-share element.
 *
 * @param <PlainT> type of underlying plain value, i.e., the value type we use for arithmetic.
 */
public class Spdz2kSInt<PlainT extends CompUInt<?, ?, PlainT>> implements SInt {

  private final PlainT share;
  private final PlainT macShare;

  /**
   * Creates a {@link Spdz2kSInt}.
   */
  public Spdz2kSInt(PlainT share, PlainT macShare) {
    this.share = share;
    this.macShare = macShare;
  }

  /**
   * Creates a {@link Spdz2kSInt} from a public value. <p>All parties compute the mac share of the
   * value but only party one (by convention) stores the public value as the share, the others store
   * 0.</p>
   */
  public Spdz2kSInt(PlainT share, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    this(isPartyOne ? share : zero, share.multiply(macKeyShare));
  }

  /**
   * Compute sum of this and other.
   */
  public Spdz2kSInt<PlainT> add(Spdz2kSInt<PlainT> other) {
    return new Spdz2kSInt<>(share.add(other.share), macShare.add(other.macShare));
  }

  /**
   * Compute difference of this and other.
   */
  public Spdz2kSInt<PlainT> subtract(Spdz2kSInt<PlainT> other) {
    return new Spdz2kSInt<>(share.subtract(other.share), macShare.subtract(other.macShare));
  }

  /**
   * Compute product of this and constant (open) value.
   */
  public Spdz2kSInt<PlainT> multiply(PlainT other) {
    return new Spdz2kSInt<>(share.multiply(other), macShare.multiply(other));
  }

  @Override
  public String toString() {
    return "Spdz2kSInt{" +
        "share=" + share +
        ", macShare=" + macShare +
        '}';
  }

  /**
   * Compute sum of this and constant (open) value. <p>All parties compute their mac share of the
   * public value and add it to the mac share of the authenticated value, however only party 1 adds
   * the public value to is value share.</p>
   *
   * @param other constant, open value
   * @param macKeyShare mac key share for maccing open value
   * @param zero zero value
   * @param isPartyOne used to ensure that only one party adds value to share
   * @return result of sum
   */
  public Spdz2kSInt<PlainT> addConstant(
      PlainT other, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    Spdz2kSInt<PlainT> wrapped = new Spdz2kSInt<>(other, macKeyShare, zero, isPartyOne);
    return add(wrapped);
  }

  /**
   * Return share.
   */
  public PlainT getShare() {
    return share;
  }

  /**
   * Return mac share.
   */
  public PlainT getMacShare() {
    return macShare;
  }

  @Override
  public SInt out() {
    return this;
  }

}
