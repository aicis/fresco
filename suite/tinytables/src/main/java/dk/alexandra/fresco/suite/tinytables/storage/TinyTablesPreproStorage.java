package dk.alexandra.fresco.suite.tinytables.storage;

import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproANDProtocol;
import java.util.List;

public interface TinyTablesPreproStorage {

	public void addANDGate(TinyTablesPreproANDProtocol gate);
	
	public List<TinyTablesPreproANDProtocol> getUnprocessedANDGates();
	
	public void storeTinyTable(int id, TinyTable tinyTable);
	
	public void storeMaskShare(int id, boolean share);
	
}