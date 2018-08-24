package dk.alexandra.fresco.suite.tinytables.prepro.datatypes;

import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;

/**
 * This class represents a masked boolean value in the preprocessing phase of the TinyTables
 * protocol suite. Note that in the preprocessing phase, no values are assigned to the wires, so
 * this class only handles the players share of the masking parameter of the wire.
 *
 */
public class TinyTablesPreproSBool implements SBool {


  private final TinyTablesElement value; // Additive share of mask of this SBool

  public TinyTablesPreproSBool(TinyTablesElement value) {
    this.value = value;
  }

  /**
   * Get this players share of the mask <i>r</i> of the wire this SBool corresponds to.
   */
  public TinyTablesElement getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "TinyTablesPreproSBool[value=" + value + "]";
  }

  @Override
  public SBool out() {
    return this;
  }

}
