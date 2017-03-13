package dk.alexandra.fresco.framework.util;

import java.io.Serializable;
import java.util.BitSet;

public class BitVector implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1732919952148180819L;
	private BitSet bits;
	private int size;
	
	public BitVector(boolean[] vector) {
		BitSetUtils.fromArray(vector);
		this.size = vector.length;
	}
	
	public BitVector(int size) {
		this.bits = new BitSet(size);
	}
	
	public int getSize() {
		return this.size;
	}
	
	public boolean get(int index) {
		return this.bits.get(index);
	}
	
	public void set(int index, boolean value) {
		this.bits.set(index, value);
	}
	
	public boolean[] asBooleans() {
		return BitSetUtils.toArray(bits, size);
	}
	
	public void xor(BitVector other) {
		if (other.getSize() != this.getSize()) {
			throw new IllegalArgumentException("Vectors does not have same size");
		}
		bits.xor(other.bits);
	}
	
}
