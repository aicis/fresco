package dk.alexandra.fresco.suite.bgw.storage;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.suite.bgw.integer.BgwSInt;

public interface BgwRandomBitSupplier {

	/**
	 * Get the next bit from this supplier.
	 *
	 * @return
	 */
	public BgwSInt getNextBit();

	/**
	 * Does this supplier have more bits? Otherwise it should call
	 * {@link generateMoreBits}.
	 * 
	 * @return
	 */
	public boolean hasMoreBits();

	/**
	 * Create a protocol for creating more bits for the provider.
	 * 
	 * @return
	 */
	public ProtocolProducer generateMoreBits();

}
