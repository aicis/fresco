package dk.alexandra.fresco.suite.ninja.storage;

import java.util.Arrays;

public class PrecomputedOutputNinja {

	private byte[] table;
	
	public PrecomputedOutputNinja(byte[] table) {
		this.table = table;
	}
	
	public byte lookup(byte value) {				
		return table[value];
	}

	@Override
	public String toString() {
		return "PrecomputedOutputNinja [table=" + Arrays.toString(table) + "]";
	}
	
	
}
