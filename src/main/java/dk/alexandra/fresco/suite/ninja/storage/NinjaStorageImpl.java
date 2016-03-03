package dk.alexandra.fresco.suite.ninja.storage;

import java.util.concurrent.ConcurrentHashMap;

import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;

public class NinjaStorageImpl implements NinjaStorage {

	StreamedStorage storage;	
	ConcurrentHashMap<Integer, PrecomputedNinja> map;
	
	public NinjaStorageImpl(StreamedStorage storage) {
		this.storage = storage;
		map = new ConcurrentHashMap<>();
	}
	
	public PrecomputedInputNinja getPrecomputedInputNinja(int id) {
		return null;
	}
	
	@Override
	public byte lookupNinjaTable(int id, byte value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte lookupNinjaTable(int id, byte left, byte right) {
		return map.get(id).lookup(left, right);
	}

	@Override
	public void storeInputNinja(int id, PrecomputedInputNinja inputNinja) {
		
	}

	@Override
	public void storeNinja(int id, PrecomputedNinja ninja) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void storeOutputNinja(int id, PrecomputedOutputNinja ninja) {
		// TODO Auto-generated method stub
		
	}

	
}
