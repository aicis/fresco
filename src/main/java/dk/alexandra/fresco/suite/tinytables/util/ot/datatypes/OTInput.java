package dk.alexandra.fresco.suite.tinytables.util.ot.datatypes;

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

	private boolean x0, x1;

	public OTInput(boolean x0, boolean x1) {
		this.x0 = x0;
		this.x1 = x1;
	}

	public boolean getX0() {
		return this.x0;
	}

	public boolean getX1() {
		return this.x1;
	}

}
