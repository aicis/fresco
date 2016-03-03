package dk.alexandra.fresco.suite.ninja.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NinjaDummyStorage implements NinjaStorage{

	private Map<Integer, PrecomputedInputNinja> inputNinjas = new ConcurrentHashMap<>();
	private Map<Integer, PrecomputedNinja> ninjas = new ConcurrentHashMap<>();
	private Map<Integer, PrecomputedOutputNinja> outputNinjas = new ConcurrentHashMap<>();
	
	public NinjaDummyStorage() {
		
	}
	
	@Override
	public byte lookupNinjaTable(int id, byte left, byte right) {
		//System.out.println("Getting ninja for id "+id +" "+ninjas.get(id));
		return ninjas.get(id).lookup(left, right);
	}
	
	@Override
	public byte lookupNinjaTable(int id, byte value) {
		//System.out.println("Getting output ninja for id "+id +" "+outputNinjas.get(id));
		return outputNinjas.get(id).lookup(value);
	}

	@Override
	public PrecomputedInputNinja getPrecomputedInputNinja(int id) {
		//System.out.println("Fetching input ninja from "+id+" with thread "+ Thread.currentThread()+", and I am: " + this);
		//this is actually enough - but to check generation of input ninja's, it might be a good idea to pick line 2.
		//return new PrecomputedInputNinja((byte)0, (byte)0);
		return inputNinjas.get(id);
	}

	@Override
	public void storeInputNinja(int id, PrecomputedInputNinja inputNinja) {
		//System.out.println("Storing input ninja: " + id+", "+inputNinja+" with thread "+ Thread.currentThread()+", and I am: " + this);
		inputNinjas.put(id, inputNinja);
	}

	@Override
	public void storeNinja(int id, PrecomputedNinja ninja) {
		//System.out.println("Storing ninja for id "+ id + ": " + ninja);
		ninjas.put(id, ninja);
	}

	@Override
	public void storeOutputNinja(int id, PrecomputedOutputNinja ninja) {
		//System.out.println("Storing Output ninja for id "+ id + ": " + ninja);
		outputNinjas.put(id, ninja);
	}

}
