package dk.alexandra.fresco.suite.tinytables.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class TinyTablesTriple implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2666542565038907636L;
	private TinyTablesElement a, b, c;
	
	public static TinyTablesTriple fromShares(boolean aShare, boolean bShare, boolean cShare) {
		return new TinyTablesTriple(new TinyTablesElement(aShare), new TinyTablesElement(bShare),
				new TinyTablesElement(cShare));
	}
	
	public TinyTablesTriple(TinyTablesElement a, TinyTablesElement b, TinyTablesElement c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public TinyTablesElement getA() {
		return this.a;
	}
	
	public TinyTablesElement getB() {
		return this.b;
	}

	public TinyTablesElement getC() {
		return this.c;
	}
	
	public void setA(TinyTablesElement a) {
		this.a = a;
	}
	
	public void setB(TinyTablesElement b) {
		this.b = b;
	}
	
	public void setC(TinyTablesElement c) {
		this.c = c;
	}
	
	@Override
	public String toString() {
		return "TinyTablesTriple: " + a + ", " + b + ", " + c;
	}

	public static byte[] encode(List<TinyTablesTriple> triples) {
		if (triples.size() % 8 != 0) {
			throw new IllegalArgumentException(
					"List must have size divisble by 8 to encode it as bytes in an optimal way");
		}
		BitSet tmp = new BitSet(triples.size() * 3);
		for (int i = 0; i < triples.size(); i++) {
			TinyTablesTriple triple = triples.get(i);
			tmp.set(3*i+0, triple.getA().getShare());
			tmp.set(3*i+1, triple.getB().getShare());
			tmp.set(3*i+2, triple.getC().getShare());
		}
		return tmp.toByteArray();
	}
	
	public static List<TinyTablesTriple> decode(byte[] asBytes) {
		if (asBytes.length % 3 != 0) {
			throw new IllegalArgumentException(
					"Array must have size divisble by 3 to be able to decode it to triples in a unique way");
		}
		int size = asBytes.length * 8 / 3;
		BitSet tmp = BitSet.valueOf(asBytes);
		List<TinyTablesTriple> triples = new ArrayList<TinyTablesTriple>();
		for (int i = 0; i < size; i++) {
			boolean a = tmp.get(3*i + 0);
			boolean b = tmp.get(3*i + 1);
			boolean c = tmp.get(3*i + 2);
			TinyTablesTriple triple = new TinyTablesTriple(new TinyTablesElement(a),
					new TinyTablesElement(b), new TinyTablesElement(c));
			triples.add(triple);
		}
		return triples;
	}
	
}
