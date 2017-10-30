package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.math.BigInteger;

public interface DataSupplier {

	/**
	 * Supplies the next triple
	 * @return the next new triple
	 */
	public abstract SpdzTriple getNextTriple();

	/**
	 * Supplies the next exp pipe
	 * @return the next new exp pipe 
	 */
	public abstract SpdzSInt[] getNextExpPipe();

	/**
	 * Supplies the next inputmask for a given input player
	 * @param towardPlayerID the id of the input player
	 * @return the appropriate input mask
	 */
	public abstract SpdzInputMask getNextInputMask(int towardPlayerID);

	/**
	 * Supplies the next bit (i.e. a SpdzSInt representing a value in {0, 1})
	 * @return the next new bit
	 */
	public abstract SpdzSInt getNextBit();

	/**
	 * The modulus used for this instance of SPDZ
	 * @return a modulus
	 */
	public abstract BigInteger getModulus();

	/**
	 * Returns the Players share of the Shared Secret Key (alpha).
	 * This is never to be send to anyone else!
	 * @return a share of the key
	 */
	public abstract BigInteger getSSK();

	/**
	 * Returns the next random field element
	 * @return A SpdzSInt representing a random secret shared field element.
	 */
	public abstract SpdzSInt getNextRandomFieldElement();

	/**
	 * Kills any resources/threads used.
	 */
	public abstract void shutdown();

}