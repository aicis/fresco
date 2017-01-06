package dk.alexandra.fresco.framework.util.ot.datatypes;

import java.security.InvalidParameterException;

/**
 * <p>This class represents the input to an oblivious transfer protocol from the
 * sender.</p>
 * 
 * <p>In an oblivious transfer, the sender provides two boolean values,
 * <i>x<sub>0</sub></i> and <i>x<sub>1</sub></i>, and the receiver provides one
 * boolean value <i>&sigma; &isin; {0,1}</i>. After the protocol has finished,
 * the receiver knows <i>x<sub>&sigma;</sub></i>, but not the other input given
 * by the sender, and the sender does not know <i>&sigma;</i>.</p>
 * 
 * @author jonas
 *
 */
public class OTInput {

	private boolean[] x0, x1;

	/**
	 * Create a new {@link OTInput} where both <i>x<sub>0</sub></i> and
	 * <i>x<sub>1</sub></i> are single bits.
	 * 
	 * @param x0
	 * @param x1
	 */
	public OTInput(boolean x0, boolean x1) {
		this(new boolean[] {x0}, new boolean[] {x1});
	}

	/**
	 * Create a new {@link OTInput} where <i>x<sub>0</sub></i> and
	 * <i>x<sub>1</sub></i> are arrays of bits. Note that they should have the
	 * same length.
	 * 
	 * @param x0
	 * @param x1
	 */
	public OTInput(boolean[] x0, boolean[] x1) {
		this.x0 = x0;
		this.x1 = x1;
		if (this.x0.length != this.x1.length) {
			throw new InvalidParameterException("Inputs must have same lenght");
		}
	}

	/**
	 * Return the length of <i>x<sub>0</sub></i> and <i>x<sub>1</sub></i> (which
	 * are equal).
	 * 
	 * @return
	 */
	public int getLength() {
		return x0.length;
	}
	
	public boolean[] getX0() {
		return getX(false);
	}

	public boolean[] getX1() {
		return getX(true);
	}

	/**
	 * If selection is <code>true</code>, this method returns
	 * <i>x<sub>1</sub></i> and if selection is <code>false</code>, this method
	 * returns <i>x<sub>0</sub></i>.
	 * 
	 * @param selection
	 * @return
	 */
	public boolean[] getX(boolean selection) {
		if (selection) {
			return this.x1;
		} else {
			return this.x0;
		}
	}

}
