package dk.alexandra.fresco.suite.tinytables.datatypes;

import java.io.Serializable;

import dk.alexandra.fresco.framework.util.BitVector;


/**
 * This class implements a storage optimised way of keeping TinyTablesElements.
 * Here each is represented only by it's share in a {@link BitVector}. So if
 * using {@link setShare}, {@link getShare} on the same index will not return
 * the same TinyTablesElement but simply a TinyTablesElement with the same
 * share.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesElementVector implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3071994286120998661L;
	private BitVector values;
	
	public TinyTablesElementVector(boolean[] shares) {
		this.values = new BitVector(shares);
	}
	
	public TinyTablesElementVector(int size) {
		this.values = new BitVector(size);
	}
	
	public void setShare(int index, boolean share) {
		this.values.set(index, share);
	}
	
	public void setShare(int index, TinyTablesElement value) {
		this.values.set(index, value.getShare());
	}
	
	public TinyTablesElement get(int index) {
		return new TinyTablesElement(values.get(index));
	}
	
	public int getSize() {
		return values.getSize();
	}
	
	public static BitVector open(TinyTablesElementVector ... vectors) {
		BitVector values = new BitVector(vectors[0].getSize());
		for (TinyTablesElementVector vector : vectors) {
			values.xor(vector.values);
		}
		return values;
	}
	
}
