package dk.alexandra.fresco.suite.tinytables.storage;

import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import java.io.Serializable;

/**
 * This class handles the data which has to be carried from the preprocessing to
 * the online phase.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public interface TinyTablesStorage extends Serializable {

	/**
	 * Store a {@link TinyTable} for the protocol with the given <code>id</code>
	 * .
	 * 
	 * @param id
	 * @param table
	 */
	public void storeTinyTable(int id, TinyTable table);

	public TinyTable getTinyTable(int id);

	/**
	 * Store a boolean for the protocol with the given ID. Can be used by a
	 * player to store a mask that he has picked during preprocessing.
	 * 
	 * @param id
	 * @param r
	 */
	public void storeMaskShare(int id, TinyTablesElement r);

	public TinyTablesElement getMaskShare(int id);

}