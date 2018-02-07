package dk.alexandra.fresco.suite.marlin.datatypes;

public class MarlinInputMask<T extends BigUInt<T>> {

  private final MarlinSInt<T> maskShare;
  private final T openValue;

  public MarlinInputMask(MarlinSInt<T> maskShare) {
    this(maskShare, null);
  }

  public MarlinInputMask(MarlinSInt<T> maskShare, T openValue) {
    this.maskShare = maskShare;
    this.openValue = openValue;
  }

  public MarlinSInt<T> getMaskShare() {
    return maskShare;
  }

  public T getOpenValue() {
    return openValue;
  }

}
