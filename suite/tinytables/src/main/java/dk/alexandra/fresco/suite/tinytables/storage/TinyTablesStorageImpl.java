package dk.alexandra.fresco.suite.tinytables.storage;

import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TinyTablesStorageImpl implements TinyTablesStorage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1135044173153933992L;
	private Map<Integer, TinyTable> tinyTables = new ConcurrentHashMap<>();
	private Map<Integer, TinyTablesElement> maskShares = new ConcurrentHashMap<>();
		
	public static Map<Integer, TinyTablesStorage> instances = new HashMap<>();

	public static TinyTablesStorage getInstance(int id) {
		if (!instances.containsKey(id)) {
			instances.put(id, new TinyTablesStorageImpl());
		}
		return instances.get(id);
	}

	@Override
	public TinyTable getTinyTable(int id) {
		return tinyTables.get(id);
	}

	@Override
	public void storeTinyTable(int id, TinyTable table) {
		tinyTables.put(id, table);
	}

	@Override
	public void storeMaskShare(int id, TinyTablesElement r) {
		maskShares.put(id, r);
	}

	@Override
	public TinyTablesElement getMaskShare(int id) {
		return maskShares.get(id);
	}

}
