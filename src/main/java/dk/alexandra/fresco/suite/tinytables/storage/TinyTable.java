package dk.alexandra.fresco.suite.tinytables.storage;

import java.io.Serializable;
import java.util.Arrays;

import dk.alexandra.fresco.framework.math.Util;

/**
 * This class represents a table of booleans indexed by arrays of booleans of arbitrary size.

 * @author jonas
 *
 */
public class TinyTable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -802945096201320092L;
	private boolean[] table;
	private int size;
	
	public TinyTable(int inputs) {
		this.table = new boolean[1 << inputs];
		this.size = inputs;
	}
	
	public TinyTable(boolean[] values) {
		if ((values.length & (values.length - 1)) != 0) {
			throw new IllegalArgumentException("Array length must be a power of two");
		}
		this.table = values;
		this.size = Util.log2(values.length);
	}

	private void setValue(int index, boolean value) {
		this.table[index] = value;
	}
	
	public void setValue(boolean[] input, boolean value) {
		if (input.length != size) {
			throw new IllegalArgumentException("Input array is of wrong size");
		}

		setValue(getIndex(input), value);
	}
	
	private boolean getValue(int index) {
		return this.table[index];
	}
	
	public boolean getValue(boolean[] input) {
		if (input.length != size) {
			throw new IllegalArgumentException("Input array is of wrong size");
		}
		return getValue(getIndex(input));
	}
	
	public int getSize() {
		return this.size;
	}
	
	private int getIndex(boolean[] input) {
		if (input.length != size) {
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
