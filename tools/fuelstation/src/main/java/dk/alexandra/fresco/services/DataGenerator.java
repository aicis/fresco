package dk.alexandra.fresco.services;

import java.math.BigInteger;
import java.util.List;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;

public interface DataGenerator {

	/**
	 * 
	 * @return The modulus used
	 */
	public BigInteger getModulus();
	
	/**
	 * 
	 * @param partyId The Id of the party whose share you want.
	 * @return The share of alpha belonging to the given partyId.
	 */
	public BigInteger getAlpha(int partyId);
	
	/**
	 * Adds the given shares of triples to
	 * @param triples
	 * @throws InterruptedException
	 */
	public void addTriples(List<SpdzTriple[]> triples, int thread) throws InterruptedException;
	
	/**
	 * Fetches the next {@code amount} triples. 
	 * @param amount
	 * @param partyId The party whose shares should be given.
	 * @param thread The VM thread number
	 * @return An array containing the given number of triples.
	 */
	public SpdzTriple[] getTriples(int amount, int partyId, int thread) throws InterruptedException;
	
	public void addBits(List<SpdzSInt[]> bits, int thread) throws InterruptedException;
	
	public SpdzElement[] getBits(int amount, int partyId, int thread) throws InterruptedException;
	
	public void addExpPipes(List<SpdzSInt[][]> expPipes, int thread) throws InterruptedException;
	
	public SpdzElement[][] getExpPipes(int amount, int partyId, int thread) throws InterruptedException;

	public void addInputMasks(int i, List<SpdzInputMask[]> inpMasks, int thread) throws InterruptedException;
	
	public SpdzInputMask[] getInputMasks(int amount, int partyId, int towardsPartyId, int thread) throws InterruptedException;
}
