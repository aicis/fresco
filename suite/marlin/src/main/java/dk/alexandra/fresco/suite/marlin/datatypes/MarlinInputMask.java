package dk.alexandra.fresco.suite.marlin.datatypes;

public class MarlinInputMask<T extends BigUInt<T>> {

  private final MarlinElement<T> maskShare;
  private final T openValue;

  public MarlinInputMask(MarlinElement<T> maskShare) {
    this(maskShare, null);
  }

  public MarlinInputMask(MarlinElement<T> maskShare, T openValue) {
    this.maskShare = maskShare;
    this.openValue = openValue;
  }

  public MarlinElement<T> getMaskShare() {
    return maskShare;
  }

  public T getOpenValue() {
    return openValue;
  }

}
