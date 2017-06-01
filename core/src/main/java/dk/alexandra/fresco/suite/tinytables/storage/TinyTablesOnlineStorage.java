package dk.alexandra.fresco.suite.tinytables.storage;

import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;

public interface TinyTablesOnlineStorage {

	public TinyTable getTinyTable(int id);
	
	public boolean getShare(int id);
	
}
