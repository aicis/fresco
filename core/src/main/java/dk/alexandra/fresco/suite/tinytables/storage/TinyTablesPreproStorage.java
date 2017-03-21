package dk.alexandra.fresco.suite.tinytables.storage;

import java.util.List;

import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproANDProtocol;

public interface TinyTablesPreproStorage {

	public void addANDGate(TinyTablesPreproANDProtocol gate);
	
	public List<TinyTablesPreproANDProtocol> getUnprocessedANDGates();
	
	public void storeTinyTable(int id, TinyTable tinyTable);
	
	public void storeMaskShare(int id, boolean share);
	
}