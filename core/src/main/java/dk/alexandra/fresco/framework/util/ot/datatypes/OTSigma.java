package dk.alexandra.fresco.framework.util.ot.datatypes;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class represents the input to an oblivious transfer protocol from the
 * reveicer.</p>
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
public class OTSigma {

	private boolean sigma;

	public OTSigma(boolean sigma) {
		this.sigma = sigma;
	}
	
	public boolean getSigma() {
		return this.sigma;
	}
	
	/**
	 * Create a list of <code>OTSigma</code>'s from a list of <i>&sigma;</i>'s.
	 * 
	 * @param sigmas
	 * @return
	 */
	public static List<OTSigma> fromList(List<Boolean> sigmas) {
		List<OTSigma> out = new ArrayList<OTSigma>();
		for (boolean s : sigmas) {
			out.add(new OTSigma(s));
		}
		return out;
	}
	
	/**
	 * Given a list of OTSigmas's, this methods return a list of all
	 * <i>&sigma</i>'s.
	 * 
	 * @param inputs
	 * @return
	 */
	public static List<Boolean> getAll(List<OTSigma> sigmas) {
		List<Boolean> out = new ArrayList<Boolean>();
		for (OTSigma sigma : sigmas) {
			out.add(sigma.getSigma());
		}
		return out;
	}
}
