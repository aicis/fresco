package dk.alexandra.fresco.suite.spdz.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import java.io.Serializable;

/**
 * An inputmask for player_i is random value r 
 * shared among parties so that only player_i knows the real value r.
 */
public class SpdzInputMask implements Serializable {

  private static final long serialVersionUID = 8757701490552440720L;

  private SpdzSInt mask;
  private FieldElement realValue;

  public SpdzInputMask(SpdzSInt mask, FieldElement realValue) {
    this.mask = mask;
    this.realValue = realValue;
  }

  public SpdzInputMask(SpdzSInt mask) {
    this.mask = mask;
    this.realValue = null;
  }

  public SpdzSInt getMask() {
    return mask;
  }

  /**
   * @return For the player that owns this inputmask, the
    shared real value of the mask. Otherwise null.
   */
  public FieldElement getRealValue() {
    return realValue;
  }

  @Override
  public String toString() {
    return "SpdzInputMask [mask=" + mask + ", realValue=" + realValue + "]";
  }
}
