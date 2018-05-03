package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.value.SInt;

public abstract class Spdz2kSInt<PlainT extends CompUInt<?, ?, PlainT>> implements SInt {

  protected final PlainT share;
  protected final PlainT macShare;

  public Spdz2kSInt(PlainT share, PlainT macShare) {
    this.share = share;
    this.macShare = macShare;
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

  public byte[] serializeShareLow() {
    return share.serializeLeastSignificant();
  }

  public byte[] serializeShareWhole() {
    return share.serializeWhole();
  }

  @Override
  public SInt out() {
    return this;
  }

}
