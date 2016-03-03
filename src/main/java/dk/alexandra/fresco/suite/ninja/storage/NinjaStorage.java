package dk.alexandra.fresco.suite.ninja.storage;

public interface NinjaStorage {

	/**
	 * Looks up the table 
	 * @param id
	 * @param left
	 * @param right
	 * @return
	 */
	public byte lookupNinjaTable(int id, byte left, byte right);	
	
	/**
	 * Lookup table for single input (e.g. output protocol)
	 * @param round
	 * @param value
	 */
	public byte lookupNinjaTable(int id, byte value);
	
	public PrecomputedInputNinja getPrecomputedInputNinja(int id);

	public void storeInputNinja(int id, PrecomputedInputNinja inputNinja);
	
	public void storeNinja(int id, PrecomputedNinja ninja);

	public void storeOutputNinja(int id, PrecomputedOutputNinja ninja);
	
}