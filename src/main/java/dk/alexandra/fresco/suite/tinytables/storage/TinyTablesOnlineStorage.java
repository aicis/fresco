package dk.alexandra.fresco.suite.tinytables.storage;

public interface TinyTablesOnlineStorage {

	public TinyTable getTinyTable(int id);
	
	public boolean getShare(int id);
	
}
