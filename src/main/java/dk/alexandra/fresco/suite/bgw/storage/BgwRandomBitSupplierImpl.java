package dk.alexandra.fresco.suite.bgw.storage;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.suite.bgw.ShamirShare;
import dk.alexandra.fresco.suite.bgw.integer.BgwSInt;

public class BgwRandomBitSupplierImpl implements BgwRandomBitSupplier {

	// TODO: How do we implement this in BGW?
	
	@Override
	public BgwSInt getNextBit() {
		BgwSInt out = new BgwSInt();
		out.value = new ShamirShare(BigInteger.valueOf(0)); // Used fair dice.
															// Guaranteed to be
															// random.
		return out;
	}

	@Override
	public boolean hasMoreBits() {
		return true;
	}

	@Override
	public ProtocolProducer generateMoreBits() {
		return null;
	}

}
