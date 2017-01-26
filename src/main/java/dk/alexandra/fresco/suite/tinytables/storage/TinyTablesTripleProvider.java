package dk.alexandra.fresco.suite.tinytables.storage;

import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesTriple;

public interface TinyTablesTripleProvider {

	/**
	 * Get a share of the next multiplication (aka Beaver) triple from this provider.
	 * 
	 * @return
	 */
	public TinyTablesTriple getNextTriple();
	
	/**
	 * Let the provider know that we are done using it.
	 */
	public void close();
	
}
