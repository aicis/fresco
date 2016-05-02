/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.crypto;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.helper.bristol.BristolCircuit;
import dk.alexandra.fresco.lib.helper.bristol.BristolCircuitParser;

/**
 * Factory for various cryptographic operations.
 * 
 * The factory is based on a basic logic factory for AND, XOR, and NOT gates. It
 * reads a full description of a boolean circuit from a static file.
 * 
 * It is based on a static circuit description produced by Nigel Smart and
 * others, see https://www.cs.bris.ac.uk/Research/CryptographySecurity/MPC/. The
 * circuit has been optimized such that it contains only a small number of
 * non-linear gates (AND gates) at the cost of more linear gates (XOR, NOT)
 * gates.
 * 
 * There is a certain amount of streaming here: The circuit description is
 * loaded into memory in a streamed manner. When getNextProtocols() is invoked,
 * new Xor and And protocols and SBools are constructed in a lazy manner.
 * However, nothing is garbage collected, so at the end of the getNextGates, the
 * full circuit is present in memory.
 * 
 * OBS: A new read of the file is done at every call to the factory. Might be
 * more efficient to load file once and generate protocols based on that.
 * 
 * IMPORTANT: The circuit here ignores padding and only allows a single-block
 * input. Each circuit implements the compression function of the respective
 * hash function, where the input chaining values are fixed to the IV values of
 * the respective hash function. The input of the circuit is a full-size message
 * block, the output consists of chaining values after invocation of the
 * compression function (formatted in the same way as the digest value would be
 * in the respective hash function). This means that non-standard test vectors
 * are used, see https://www.cs.bris.ac.uk/Research/CryptographySecurity/MPC/.
 * 
 */
public class BristolCryptoFactory implements ProtocolFactory {

	private BasicLogicFactory blf;

	public BristolCryptoFactory(BasicLogicFactory blf) {
		this.blf = blf;
	}

	
	/**
	 * A circuit for 32x32 bit multiplication.
	 * 
	 * The complete circuit consists of 5926 AND gates, 1069 XOR gates, and 5379
	 * NOT gates.
	 * 
	 * @param in1,
	 *            in2 Arrays of 32 SBools for the two inputs to multiply.
	 * @param out
	 *            Array of 64 SBools for the resulting product.
	 * @return A protocol that computes 32x32 bit multiplication.
	 * 
	 */
	public BristolCircuit getMult32x32Circuit(SBool[] in1, SBool[] in2, SBool[] out) {
		if (null == in1 || in1.length != 32)
			throw new IllegalArgumentException("'in1' must be array of 32 SBools");
		if (null == in2 || in2.length != 32)
			throw new IllegalArgumentException("'in1' must be array of 32 SBools");
		if (null == out || out.length != 64)
			throw new IllegalArgumentException("'out' must be array of 64 SBools");
		BristolCircuitParser parser = BristolCircuitParser.readCircuitDescription(this.blf, "circuits/mult_32x32.txt", in1, in2, out);
		return new BristolCircuit(parser, in1, in2, out);
	}
	
	
	/**
	 * A circuit for one evaluation of the MD5 compression function.
	 * 
	 * The complete circuit consists of 29084 AND gates, 14150 XOR gates, and
	 * 34627 NOT gates.
	 * 
	 * @param in
	 *            Array of 512 SBools describing the input block to be
	 *            compressed.
	 * @param out
	 *            Array of 160 SBools describing the resulting compressed
	 *            output.
	 * @return A protocol that computes one evaluation of the MD5 compression
	 *         function
	 * 
	 */
	public BristolCircuit getMD5Circuit(SBool[] in, SBool[] out) {
		if (null == in || in.length != 512)
			throw new IllegalArgumentException("'in' must be array of 512 SBools");
		if (null == out || out.length != 128)
			throw new IllegalArgumentException("'out' must be array of 128 SBools");
		SBool[] in2= new SBool[0]; // Bristol circuits expects two inputs.
		BristolCircuitParser parser = BristolCircuitParser.readCircuitDescription(this.blf, "circuits/md5.txt", in, in2, out);
		return new BristolCircuit(parser, in, in2, out);
	}
	
	
	/**
	 * The complete circuit consists of 37300 AND gates, 24166 XOR gates, and
	 * 45135 NOT gates.
	 * 
	 * @param in
	 *            Array of 512 SBools describing the input block to be
	 *            compressed.
	 * @param out
	 *            Array of 160 SBools describing the resulting compressed
	 *            output.
	 * @return A protocol that computes one evaluation of the SHA-1 compression
	 *         function.
	 * 
	 */
	public BristolCircuit getSha1Circuit(SBool[] in, SBool[] out) {
		if (null == in || in.length != 512)
			throw new IllegalArgumentException("'in' must be array of 512 SBools");
		if (null == out || out.length != 160)
			throw new IllegalArgumentException("'out' must be array of 160 SBools");
		SBool[] in2= new SBool[0]; // Bristol circuits expects two inputs.
		BristolCircuitParser parser = BristolCircuitParser.readCircuitDescription(this.blf, "circuits/sha-1.txt", in, in2, out);
		return new BristolCircuit(parser, in, in2, out);
	}
	
	
	/**
	 * The complete circuit consists of 90825 AND gates, 42029 XOR gates, and
	 * 103258 NOT gates.
	 * 
	 * @param in
	 *            Array of 512 SBools describing the input block to be
	 *            compressed.
	 * @param out
	 *            Array of 160 SBools describing the resulting compressed
	 *            output.
	 * @return A protocol that computes SHA-256.
	 * 
	 */
	public BristolCircuit getSha256Circuit(SBool[] in, SBool[] out) {
		if (null == in || in.length != 512)
			throw new IllegalArgumentException("'in' must be array of 512 SBools");
		if (null == out || out.length != 256)
			throw new IllegalArgumentException("'out' must be array of 256 SBools");
		SBool[] in2= new SBool[0]; // Bristol circuits expects two inputs.
		BristolCircuitParser parser = BristolCircuitParser.readCircuitDescription(this.blf, "circuits/sha-256.txt", in, in2, out);
		return new BristolCircuit(parser, in, in2, out);
	}

	
	/**
	 * A circuit for computing one 128-bit AES encryption.
	 * 
	 * The circuit includes expansion of the 128-bit key.
	 * 
	 * The circuit contains 6800 AND gates, 25124 XOR gates, and 1692 NOT gates.
	 * 
	 * NOTE: This is not the most efficient AES circuit (with regards to AND
	 * gates). A more efficent circuit has been produced (see somewhere on
	 * Cryptology ePrint Archive), but the circuit used here has nevertheless
	 * become a common benchmark reference.
	 * 
	 * @param key Array of 128 SBools representing the key used for encryption.
	 * @param kkkkey The 128-bit plaintext to encrypt (as a SBool array).
	 * @param ciphertext The resulting 128-bit ciphertext (as a SBool array).
	 * @return A protocol that computes one AES encryption.
	 * 
	 */
	public BristolCircuit getAesProtocol(SBool[] plaintext, SBool[] key, SBool[] ciphertext) {
		if (null == key || key.length != 128)
			throw new IllegalArgumentException("Key must be array of 128 SBools");
		if (null == plaintext || plaintext.length != 128)
			throw new IllegalArgumentException("Plaintext must be array of 128 SBools");
		if (null == ciphertext || ciphertext.length != 128)
			throw new IllegalArgumentException("Ciphertext must be array of 128 SBools");
		BristolCircuitParser parser = BristolCircuitParser.readCircuitDescription(this.blf, "circuits/AES-non-expanded.txt", plaintext, key, ciphertext);
		return new BristolCircuit(parser, plaintext, key, ciphertext);
	}
	
	
	/**
	 * A circuit for computing one DES block encryption.
	 * 
	 * The circuit includes expansion of the input key.
	 * 
	 * The circuit consists of 18124 AND gates, 1340 XOR gates and 10849 NOT gates.
	 * 
	 * @param key Array of 64 SBools representing the key used for encryption.
	 * @param plaintext The 64-bit plaintext to encrypt (as a SBool array).
	 * @param ciphertext The resulting 64-bit ciphertext (as a SBool array).
	 * @return A protocol that computes one DES encryption.
	 * 
	 */
	public BristolCircuit getDesCircuit(SBool[] plaintext, SBool[] key, SBool[] ciphertext) {
		if (null == plaintext || plaintext.length != 64)
			throw new IllegalArgumentException("Plaintext must be array of 64 SBools");
		if (null == key || key.length != 64)
			throw new IllegalArgumentException("Key must be array of 64 SBools");
		if (null == ciphertext || ciphertext.length != 64)
			throw new IllegalArgumentException("Ciphertext must be array of 64 SBools");
		BristolCircuitParser parser = BristolCircuitParser.readCircuitDescription(this.blf, "circuits/DES-non-expanded.txt", plaintext, key, ciphertext);
		return new BristolCircuit(parser, plaintext, key, ciphertext);
	}
	
}
