package dk.alexandra.fresco.services;

import java.io.IOException;
import java.math.BigInteger;

public interface Preprocessor {

	/**
	 * Returns the next amount of secret shared triples belonging to the partyId.
	 * @param amount The amount of triples to return.
	 * @param partyId The ID of the party who should receive the triples.
	 * @return The byte[] representing the serialized secret shared triples.
	 * @throws IOException if the stream fails or is closed early. 
	 */
	public byte[] getTriples(int amount, int partyId) throws IOException;
	
	/**
	 * Returns the next amount of secret shared input masks belonging to the partyId which works if the party with id towardsPartyId inputs. 
	 * @param amount The amount of input masks to return.
	 * @param partyId The ID of the party who should receive the input masks.
	 * @param towardsPartyId The ID of the party wanting to input data.
	 * @return The byte[] representing the serialized secret shared input masks.
	 * @throws IOException If the stream fails or is closed early.
	 */
	public byte[] getInputMasks(int amount, int partyId, int towardsPartyId) throws IOException;
	
	/**
	 * Returns the next amount of secret shared bits belonging to the partyId.
	 * @param amount The amount of bits to return.
	 * @param partyId The ID of the party who should receive the bits.
	 * @return The byte[] representing the serialized secret shared bits
	 * @throws IOException if the stream fails or is closed early. 
	 */
	public byte[] getBits(int amount, int partyId) throws IOException;
	
	/**
	 * Returns the next amount of secret shared exponentiation pipes belonging to the partyId.
	 * @param amount The amount of exponentiation pipes to return.
	 * @param partyId The ID of the party who should receive the exponentiation pipes.
	 * @return The byte[] representing the serialized secret shared exponentiation pipes
	 * @throws IOException if the stream fails or is closed early. 
	 */
	public byte[] getExpPipes(int amount, int partyId) throws IOException;
	
	/**
	 * Returns the modulus used to generate the preprocessed material.
	 * @return The global modulus.
	 */
	public BigInteger getModulus();
	
	/**
	 * Returns the partyId's share of the secret key.
	 * @return The partyId's share of the secret key.
	 */
	public BigInteger alpha(int partyId);
}
