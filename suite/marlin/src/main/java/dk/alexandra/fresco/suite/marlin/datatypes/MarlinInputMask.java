package dk.alexandra.fresco.suite.marlin.datatypes;

public class MarlinInputMask<H extends UInt<H>, L extends UInt<L>, T extends CompUInt<H, L, T>> {

  private final MarlinSInt<H, L, T> maskShare;
  private final T openValue;

  public MarlinInputMask(MarlinSInt<H, L, T> maskShare) {
    this(maskShare, null);
  }

  public MarlinInputMask(MarlinSInt<H, L, T> maskShare, T openValue) {
    this.maskShare = maskShare;
    this.openValue = openValue;
  }

  public MarlinSInt<H, L, T> getMaskShare() {
    return maskShare;
  }

  public T getOpenValue() {
    return openValue;
  }

}
