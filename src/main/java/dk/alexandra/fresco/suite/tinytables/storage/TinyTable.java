package dk.alexandra.fresco.suite.tinytables.storage;

import java.io.Serializable;
import java.util.Arrays;

import dk.alexandra.fresco.framework.math.Util;

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
	private boolean[] table;
	private int dimension;

	/**
	 * Create a new TinyTable with the given values. Here the convention is that
	 * the entry with index <i>[b<sub>0</sub>, b<sub>1</sub>, ...,
	 * b<sub>n-1</sub>]</i> corresponds to the integer index <i>b<sub>0</sub> +
	 * b<sub>1</sub> 2 + ... + b<sub>n-1</sub> 2<sup>n-1</sup></i> when
	 * considering the boolean values <i>b<sub>0</sub>, b<sub>1</sub>, ...,
	 * b<sub>n-1</sub></i> as each being either <i>0</i> or <i>1</i>.
	 * <p>
	 * Note that to create a TinyTable that takes <i>n</i> booleans as inputs,
	 * the array need to be of length <i>2<sup>n</sup></i>.
	 * </p>
	 * 
	 * @param values
	 */
	public TinyTable(boolean[] values) {
		if (!Util.isPowerOfTwo(values.length)) {
			throw new IllegalArgumentException("Array length must be a power of two");
		}
		this.table = values;
		this.dimension = Util.log2(values.length);
	}

	private boolean getValue(int index) {
		return this.table[index];
	}

	/**
	 * Return the entry for this TinyTable corresponding to the given values of
	 * inputs.
	 * 
	 * @param input
	 * @return
	 */
	public boolean getValue(boolean... input) {
		if (input.length != dimension) {
			throw new IllegalArgumentException("Input array is of wrong size");
		}
		return getValue(getIndex(input));
	}

	/**
	 * Return the number of boolean inputs used for this TinyTable.
	 * 
	 * @return
	 */
	public int getNumberOfInputs() {
		return this.dimension;
	}

	/**
	 * Calculate the index used for the internal storage of the TinyTable which
	 * is a one-dimensional boolean array. It is done by considering the input
	 * as a base 2 expansion of an integer and returning this integer.
	 * 
	 * @param input
	 * @return
	 */
	private int getIndex(boolean[] input) {
		if (input.length != dimension) {
			throw new IllegalArgumentException("Input array is of wrong size");
		}

		int index = 0;
		for (int i = 0; i < input.length; i++) {
			int base = 1 << (input.length - i - 1);
			index += input[i] ? base : 0;
		}
		return index;
	}

	@Override
	public String toString() {
		return Arrays.toString(table);
	}
	
}
