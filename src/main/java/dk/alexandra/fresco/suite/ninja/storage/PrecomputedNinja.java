package dk.alexandra.fresco.suite.ninja.storage;

public class PrecomputedNinja {

	private byte[] table;
	
	public PrecomputedNinja(byte[] table) {
		this.table = table;
	}
	
	public byte lookup(byte left, byte right) {
		byte shifted = (byte) (left << 1); 
		byte lookup = (byte) (shifted ^ right);		
		return table[lookup];
	}
}
