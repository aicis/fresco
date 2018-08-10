package dk.alexandra.fresco.suite.tinytables.online.datatypes;

import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;

/**
 * This class represents a masked boolean value in the online phase of the TinyTables protocol. The
 * two players both know the masked value, <i>e = r + b</i>, but each player only knows his share of
 * the value <i>e</i> (and of the mask <i>r</i>, which was picked during the preprocessing phase).
 *
 */
public class TinyTablesSBool implements SBool {

  private final TinyTablesElement value;
  private static final TinyTablesSBool TRUE = new TinyTablesSBool(TinyTablesElement.getTinyTablesElement(true));
  private static final TinyTablesSBool FALSE = new TinyTablesSBool(TinyTablesElement.getTinyTablesElement(false));

  private TinyTablesSBool(TinyTablesElement share) {
    this.value = share;
  }

  public TinyTablesElement getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "TinyTablesSBool [value=" + value + "]";
  }

  @Override
  public SBool out() {
    return this;
  }

  public static TinyTablesSBool getTinyTablesSBool(TinyTablesElement share) {
    return share.getShare() ? TRUE : FALSE;
  }

}
