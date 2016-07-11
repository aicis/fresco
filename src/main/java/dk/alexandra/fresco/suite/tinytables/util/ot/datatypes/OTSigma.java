package dk.alexandra.fresco.suite.tinytables.util.ot.datatypes;

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
	
}
