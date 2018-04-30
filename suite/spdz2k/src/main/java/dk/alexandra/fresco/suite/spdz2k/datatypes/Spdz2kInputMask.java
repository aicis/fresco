package dk.alexandra.fresco.suite.spdz2k.datatypes;

public class Spdz2kInputMask<PlainT extends CompUInt<?, ?, PlainT>> {

  private final Spdz2kSIntArithmetic<PlainT> maskShare;
  private final PlainT openValue;

  public Spdz2kInputMask(Spdz2kSIntArithmetic<PlainT> maskShare) {
    this(maskShare, null);
  }

  public Spdz2kInputMask(Spdz2kSIntArithmetic<PlainT> maskShare, PlainT openValue) {
    this.maskShare = maskShare;
    this.openValue = openValue;
  }

  public Spdz2kSIntArithmetic<PlainT> getMaskShare() {
    return maskShare;
  }

  public PlainT getOpenValue() {
    return openValue;
  }

}
