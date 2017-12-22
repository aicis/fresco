package dk.alexandra.fresco.tools.mascot.field;

public class InputMask {

  private final FieldElement openValue;
  private final AuthenticatedElement maskShare;

  /**
   * Creates new {@link InputMask}.
   * 
   * @param openValue the actual value
   * @param maskShare the authenticated share of the value
   */
  public InputMask(FieldElement openValue, AuthenticatedElement maskShare) {
    super();
    this.openValue = openValue;
    this.maskShare = maskShare;
  }

  public InputMask(AuthenticatedElement maskShare) {
    this(null, maskShare);
  }

  public FieldElement getOpenValue() {
    return openValue;
  }

  public AuthenticatedElement getMaskShare() {
    return maskShare;
  }
  
}
