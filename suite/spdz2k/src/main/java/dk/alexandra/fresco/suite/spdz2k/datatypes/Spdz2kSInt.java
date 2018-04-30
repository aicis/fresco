package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.value.SInt;

public abstract class Spdz2kSInt<PlainT extends CompUInt<?, ?, PlainT>> implements SInt {

  protected final PlainT share;
  protected final PlainT macShare;

  public Spdz2kSInt(PlainT macShare, PlainT share) {
    this.macShare = macShare;
    this.share = share;
  }

  /**
   * Compute sum of this and other.
   */
  public Spdz2kSIntArithmetic<PlainT> add(Spdz2kSIntArithmetic<PlainT> other) {
    return new Spdz2kSIntArithmetic<>(share.add(other.share), macShare.add(other.macShare));
  }

  /**
   * Compute difference of this and other.
   */
  public Spdz2kSIntArithmetic<PlainT> subtract(Spdz2kSIntArithmetic<PlainT> other) {
    return new Spdz2kSIntArithmetic<>(share.subtract(other.share),
        macShare.subtract(other.macShare));
  }

  /**
   * Compute product of this and constant (open) value.
   */
  public Spdz2kSIntArithmetic<PlainT> multiply(PlainT other) {
    return new Spdz2kSIntArithmetic<>(share.multiply(other), macShare.multiply(other));
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
  public Spdz2kSIntArithmetic<PlainT> addConstant(
      PlainT other, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    Spdz2kSIntArithmetic<PlainT> wrapped = new Spdz2kSIntArithmetic<>(other, macKeyShare, zero,
        isPartyOne);
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

  public abstract byte[] serializeShareLow();

  public abstract byte[] serializeShareWhole();

  @Override
  public SInt out() {
    return this;
  }

}
