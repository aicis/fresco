package dk.alexandra.fresco.suite.tinytables.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import java.io.Serializable;
import java.util.Arrays;

/**
 * <p>
 * This class represents a table of booleans indexed by arrays of booleans of a
 * fixed size.
 * </p>
 *
 * <p>
 * In the TinyTables specifications, only TinyTables of dimension 2 is used, but
 * we allow arbitrary dimension since we allow protocols to take an arbitrary
 * number of inputs.
 * </p>
 *
 */
public class TinyTable implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -8858300334880894784L;
  private TinyTablesElement[][] table;
  private FieldElement[][] macTable;

  public TinyTable(TinyTablesElement[] values, FieldElement[] macValues) {
    if (values.length != 4) {
      throw new IllegalArgumentException("Array length must be 4");
    }
    this.table = new TinyTablesElement[2][2];
    this.table[0][0] = values[0];
    this.table[0][1] = values[1];
    this.table[1][0] = values[2];
    this.table[1][1] = values[3];
    if (macValues.length != 4) {
      throw new IllegalArgumentException("Array length must be 4");
    }
    this.macTable = new FieldElement[2][2];
    this.macTable[0][0] = macValues[0];
    this.macTable[0][1] = macValues[1];
    this.macTable[1][0] = macValues[2];
    this.macTable[1][1] = macValues[3];
  }


  /**
   * Return the entry for this TinyTable corresponding to the given values of
   * inputs.
   *
   * @return
   */
  public TinyTablesElement getValue(TinyTablesElement eu, TinyTablesElement ev) {
    return table[asInt(eu.getShare())][asInt(ev.getShare())];
  }
  /**
   * Return the entry for the table of macs corresponding to the given values of
   * inputs.
   *
   * @return
   */
  public FieldElement getMacValue(TinyTablesElement eu, TinyTablesElement ev) {
    return macTable[asInt(eu.getShare())][asInt(ev.getShare())];
  }

  @Override
  public String toString() {
    //TODO: add macs
    return Arrays.deepToString(table);
  }

  private int asInt(boolean b) {
    return b ? 1 : 0;
  }

}
