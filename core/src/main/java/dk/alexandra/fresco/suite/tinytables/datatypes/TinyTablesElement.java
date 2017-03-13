package dk.alexandra.fresco.suite.tinytables.datatypes;

import java.io.Serializable;
import java.util.List;

import dk.alexandra.fresco.framework.util.Pair;

/**
 * Instances of this class represents an additive share of a boolean value.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesElement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1743789194879077877L;
	private boolean share;

	public TinyTablesElement() {
		this.share = false;
	}

	public TinyTablesElement(boolean share) {
		this.share = share;
	}

	public boolean getShare() {
		return share;
	}

	public void setShare(boolean share) {
		this.share = share;
	}

	public static boolean open(List<TinyTablesElement> allShares) {
		boolean res = false;
		for (TinyTablesElement share : allShares) {
			res ^= share.share;
		}
		return res;
	}

	/**
	 * Returns a share of <i>a + b</i> assuming that the given parameter
	 * represents a share of <i>a</i>.
	 * 
	 * @param other
	 * @return
	 */
	public TinyTablesElement add(TinyTablesElement b) {
		return new TinyTablesElement(this.share ^ b.share);
	}

	/**
	 * Returns a share of this with a known boolean s.
	 * 
	 * @param s
	 * @return
	 */
	public TinyTablesElement multiply(boolean s) {
		return new TinyTablesElement(s & this.share);
	}
	
	public TinyTablesElement not(int playerId) {
		if (playerId == 1) {
			return flip();
		}
		return new TinyTablesElement(this.share);
	}
	
	/**
	 * Flips this share. Note that if both (or more precisely, an even number
	 * of) parties does this, the shared value is not changed.
	 * 
	 * @return
	 */
	public TinyTablesElement flip() {
		return new TinyTablesElement(!this.share);
	}
	
	/**
	 * Perform the first round of a multiplicaiton of two additively shared
	 * values (this and other). A {@link TinyTablesTriple}, </i>(a,b,c)</i>
	 * should be provided, and this method will in turn return shares of two
	 * values, <i>e = this - a</i> and <i>d = other - b</i>.
	 * 
	 * In order to finialize this, the opened values of e and d should be
	 * provided along with the used triple to {@link multiplyFinalize} in order
	 * to get the result.
	 * 
	 * @param other
	 * @param triple
	 * @return
	 */
	public Pair<TinyTablesElement, TinyTablesElement> multiply(TinyTablesElement other,
			TinyTablesTriple triple) {
		TinyTablesElement e = this.add(triple.getA());
		TinyTablesElement d = other.add(triple.getB());
		return new Pair<TinyTablesElement, TinyTablesElement>(e, d);
	}
	
	/**
	 * Finalize a multiplication started by a call to {@link multiply}.
	 * 
	 * @param e
	 *            The opened value of the first element returned by
	 *            {@link #multiply(TinyTablesElement, TinyTablesTriple)}.
	 * @param d
	 *            The opened value of the second element returned by
	 *            {@link #multiply(TinyTablesElement, TinyTablesTriple)}.
	 * @param triple
	 *            The triple used in
	 *            {@link #multiply(TinyTablesElement, TinyTablesTriple)}.
	 * @param playerId
	 * @return
	 */
	public static TinyTablesElement finalizeMultiplication(boolean e, boolean d,
			TinyTablesTriple triple, int playerId) {
		TinyTablesElement product = triple.getC().add(triple.getB().multiply(e))
				.add(triple.getA().multiply(d));
		if (playerId == 1) {
			product = product.add(new TinyTablesElement(e & d));
		}
		return product;
	}

}
