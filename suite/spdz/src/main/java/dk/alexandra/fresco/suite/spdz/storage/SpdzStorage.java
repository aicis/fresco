package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import java.math.BigInteger;
import java.util.List;


/**
 * Manages the storage associated with the online phase of SPDZ. This includes all the preprocessed data and the opened and 
 * closed accumulated during the online phase
 *
 */
public interface SpdzStorage{

	/**
	 * Attempts to shutdown the storage nicely
	 */
	public abstract void shutdown();

	/**
	 * Resets the opened and closed values
	 */
	public abstract void reset();

	/**
	 * Gets a data supplier suppling preprocessed data values
	 * @return a data supplier
	 */
	public abstract DataSupplier getSupplier();

	/**
	 * Adds an opened value
	 * @param val a value to be added
	 */
	public abstract void addOpenedValue(BigInteger val);

	/**
	 * Adds a closed values
	 * @param elem a element to add
	 */
	public abstract void addClosedValue(SpdzElement elem);

	/**
	 * Get the current opened values
	 * @return a list of opened values
	 */
	public abstract List<BigInteger> getOpenedValues();

	/**
	 * Get the current closed values
	 * @return a list of closed values
	 */
	public abstract List<SpdzElement> getClosedValues();

	/**
	 * Returns the players share of the Secret Shared Key (alpha). 
	 * @return alpha_i
	 */
	public abstract BigInteger getSSK();

}