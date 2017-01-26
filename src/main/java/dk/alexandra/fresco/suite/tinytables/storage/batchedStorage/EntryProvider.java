package dk.alexandra.fresco.suite.tinytables.storage.batchedStorage;

import java.util.List;

public interface EntryProvider<T> {

	public List<T> get(int amount);
	
}
