package dk.alexandra.fresco.suite.tinytables.util.ot.datatypes;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

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

	public OTInput(boolean x0, boolean x1) {
		this(new boolean[] {x0}, new boolean[] {x1});
	}

	public OTInput(boolean[] x0, boolean[] x1) {
		this.x0 = x0;
		this.x1 = x1;
		if (this.x0.length != this.x1.length) {
			throw new InvalidParameterException("Inputs must have same lenght");
		}
	}

	public int getLength() {
		return x0.length;
	}
	
	public boolean[] getX0() {
		return this.x0;
	}

	public boolean[] getX1() {
		return this.x1;
	}

	/**
	 * Create a list of <code>OTInput</code>'s from lists of <i>x<sub>0</sub></i>'s and
	 * <i>x<sub>1</sub></i>'s of length 1.
	 * 
	 * @param x0s
	 * @param x1s
	 * @return
	 */
	public static List<OTInput> fromLists(List<Boolean> x0s, List<Boolean> x1s) {
		if (x0s.size() != x1s.size()) {
			throw new InvalidParameterException("Must have same number of x0s and x1s");
		}
		List<OTInput> out = new ArrayList<OTInput>();
		for (int i = 0; i < x0s.size(); i++) {
			out.add(new OTInput(x0s.get(i), x1s.get(i)));
		}
		return out;
	}
	
	/**
	 * Given a list of OTInput's, this methods return a list of all
	 * <i>x<sub>i</sub></i> from the inputs where <i>i = 0,1</i> is the specified
	 * index.
	 * 
	 * @param inputs
	 * @param index
	 * @return
	 */
	public static List<boolean[]> getAll(List<OTInput> inputs, int index) {
		if (index < 0 || index > 1) {
			throw new InvalidParameterException("Index must be either 0 or 1");
		}
		List<boolean[]> out = new ArrayList<boolean[]>();
		for (OTInput input : inputs) {
			if (index == 0) {
				out.add(input.getX0());
			} else {
				out.add(input.getX1());
			}
		}
		return out;
	}
}
