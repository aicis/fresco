package dk.alexandra.fresco.suite.tinytables.online.datatypes;

import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.ot.Encoding;

/**
 * This class represents a masked boolean value in the online phase of the TinyTables protocol. The
 * two players both know the masked value, <i>e = r + b</i>, but each player only knows his share of
 * the value <i>e</i> (and of the mask <i>r</i>, which was picked during the preprocessing phase).
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesSBool implements SBool {


  private TinyTablesElement value;

  public TinyTablesSBool() {}

  public TinyTablesSBool(TinyTablesElement share) {
    this.value = share;
  }

  public TinyTablesElement getValue() {
    return value;
  }

  public void setValue(TinyTablesElement share) {
    this.value = share;
  }

  @Override
  public String toString() {
    return "TinyTablesSBool [value=" + value + "]";
  }

  public byte[] getSerializableContent() {
    return new byte[] {Encoding.encodeBoolean(value.getShare())};
  }

  public void setSerializableContent(byte[] val) {
    boolean share = Encoding.decodeBoolean(val[0]);
    this.setValue(new TinyTablesElement(share));
  }

  @Override
  public SBool out() {
    return this;
  }

}
