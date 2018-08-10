package dk.alexandra.fresco.suite.tinytables.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class TinyTablesTriple implements Serializable {

	/**
   *
   */
  private static final long serialVersionUID = 7661693903147118861L;
  private final TinyTablesElement a, b, c;
  private static final TinyTablesTriple trip0 = new TinyTablesTriple(false, false, false);
  private static final TinyTablesTriple trip1 = new TinyTablesTriple(false, false, true);;
  private static final TinyTablesTriple trip2 = new TinyTablesTriple(false, true, false);;
  private static final TinyTablesTriple trip3 = new TinyTablesTriple(false, true, true);;
  private static final TinyTablesTriple trip4 = new TinyTablesTriple(true, false, false);;
  private static final TinyTablesTriple trip5 = new TinyTablesTriple(true, false, true);;
  private static final TinyTablesTriple trip6 = new TinyTablesTriple(true, true, false);;
  private static final TinyTablesTriple trip7 = new TinyTablesTriple(true, true, true);;

	public static TinyTablesTriple fromShares(boolean aShare, boolean bShare, boolean cShare) {
	  //return new TinyTablesTriple(aShare, bShare, cShare);
	  int index = (aShare ? 4 : 0) + (bShare ? 2 : 0) + (cShare ? 1 : 0);
    switch (index) {
      case 0:
        return trip0;
      case 1:
        return trip1;
      case 2:
        return trip2;
      case 3:
        return trip3;
      case 4:
        return trip4;
      case 5:
        return trip5;
      case 6:
        return trip6;
      case 7:
        return trip7;
      default:
        throw new IllegalStateException("This cannot happen");
    }
	}

	private TinyTablesTriple(TinyTablesElement a, TinyTablesElement b, TinyTablesElement c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	private TinyTablesTriple(boolean a, boolean b, boolean c) {
	  this(TinyTablesElement.getTinyTablesElement(a), TinyTablesElement.getTinyTablesElement(b),
	      TinyTablesElement.getTinyTablesElement(c));
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

	@Override
	public String toString() {
		return "TinyTablesTriple:(" + a + "," + b + "," + c + ")";
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
		List<TinyTablesTriple> triples = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			boolean a = tmp.get(3*i + 0);
			boolean b = tmp.get(3*i + 1);
			boolean c = tmp.get(3*i + 2);
			TinyTablesTriple triple = new TinyTablesTriple(TinyTablesElement.getTinyTablesElement(a),
					TinyTablesElement.getTinyTablesElement(b), TinyTablesElement.getTinyTablesElement(c));
			triples.add(triple);
		}
		return triples;
	}

}
