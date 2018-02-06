package dk.alexandra.fresco.suite.tinytables.datatypes;

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
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -802945096201320092L;
	private TinyTablesElement[][] table;

	public TinyTable(TinyTablesElement ... values) {
		if (values.length != 4) {
			throw new IllegalArgumentException("Array length must be 4");
		}
		this.table = new TinyTablesElement[2][2];
		this.table[0][0] = values[0];
		this.table[0][1] = values[1];
		this.table[1][0] = values[2];
		this.table[1][1] = values[3];
	}


	/**
	 * Return the entry for this TinyTable corresponding to the given values of
	 * inputs.
	 * 
	 * @param input
	 * @return
	 */
	public TinyTablesElement getValue(TinyTablesElement eu, TinyTablesElement ev) {
		return table[asInt(eu.getShare())][asInt(ev.getShare())];
	}

	@Override
	public String toString() {
		return Arrays.toString(table);
	}
	
	private int asInt(boolean b) {
		return b ? 1 : 0;
	}
	
}
