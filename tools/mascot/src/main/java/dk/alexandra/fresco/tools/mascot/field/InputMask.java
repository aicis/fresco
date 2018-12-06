package dk.alexandra.fresco.tools.mascot.field;

/**
 * An input mask for player_i is random value r shared among parties so that only player_i knows the
 * real value r.
 */
public class InputMask {

  private final MascotFieldElement openValue;
  private final AuthenticatedElement maskShare;

  /**
   * Creates new {@link InputMask}.
   *
   * @param openValue the actual value
   * @param maskShare the authenticated share of the value
   */
  public InputMask(MascotFieldElement openValue, AuthenticatedElement maskShare) {
    this.openValue = openValue;
    this.maskShare = maskShare;
  }

  public InputMask(AuthenticatedElement maskShare) {
    this(null, maskShare);
  }

  public MascotFieldElement getOpenValue() {
    return openValue;
  }

  public AuthenticatedElement getMaskShare() {
    return maskShare;
  }

}
