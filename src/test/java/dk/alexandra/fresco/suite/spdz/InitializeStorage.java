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
package dk.alexandra.fresco.suite.spdz;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;
import dk.alexandra.fresco.suite.spdz.storage.d142.NewSpdzStorageConstants;

public class InitializeStorage {

	/**
	 * Generates on runtime the necessary preprocessed data for SPDZ tests and
	 * stores it in the different stores that were given as argument.
	 * 
	 * @param stores
	 * @param noOfPlayers
	 * @param noOfTriples
	 * @param noOfInputMasks
	 * @param noOfBits
	 */
	public static void initStorage(Storage[] stores, int noOfPlayers, int noOfTriples, int noOfInputMasks, int noOfBits, int noOfExpPipes) {		

		List<Storage> tmpStores = new ArrayList<Storage>();
		for (Storage s : stores) {
			if (s.getObject(NewSpdzStorageConstants.STORAGE_NAME_PREFIX + 1,
					NewSpdzStorageConstants.MODULUS_KEY) == null) {
				tmpStores.add(s);
			}
		}
		Storage[] storages = tmpStores.toArray(new Storage[0]);

		BigInteger p = new BigInteger(
				"6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
		
		List<BigInteger> alphaShares = FakeTripGen.generateAlphaShares(noOfPlayers, p);
		BigInteger alpha = BigInteger.ZERO;
		for(BigInteger share : alphaShares) {
			alpha = alpha.add(share);
		}
		alpha = alpha.mod(p);
		
		List<SpdzTriple[]> triples = FakeTripGen.generateTriples(noOfTriples,
				noOfPlayers, p, alpha);
		List<List<SpdzInputMask[]>> inputMasks = FakeTripGen
				.generateInputMasks(noOfInputMasks, noOfPlayers, p, alpha);
		List<SpdzSInt[]> bits = FakeTripGen.generateBits(noOfBits, noOfPlayers, p,
				alpha);
		List<SpdzSInt[][]> expPipes = FakeTripGen.generateExpPipes(noOfExpPipes, noOfPlayers, p, alpha);
		

		for (dk.alexandra.fresco.framework.sce.resources.storage.Storage store : storages) {
			for (int i = 1; i < noOfPlayers + 1; i++) {
				String storageName = NewSpdzStorageConstants.STORAGE_NAME_PREFIX
						+ i;
				store.putObject(storageName,
						NewSpdzStorageConstants.MODULUS_KEY, p);
				store.putObject(storageName,
						NewSpdzStorageConstants.SSK_KEY, alphaShares.get(i-1));
			}
			// triples
			int tripleCounter = 0;
			for (SpdzTriple[] triple : triples) {
				for (int i = 0; i < noOfPlayers; i++) {
					String storageName = NewSpdzStorageConstants.STORAGE_NAME_PREFIX
							+ (i + 1);
					store.putObject(storageName,
							NewSpdzStorageConstants.TRIPLE_KEY_PREFIX
									+ tripleCounter, triple[i]);
				}
				tripleCounter++;
			}
			// inputs
			// towards player
			for (int towardsPlayer = 1; towardsPlayer < inputMasks.size() + 1; towardsPlayer++) {
				int[] inputCounters = new int[noOfPlayers];
				// number of inputs towards that player
				for (SpdzInputMask[] masks : inputMasks.get(towardsPlayer - 1)) {
					// single shares of that input
					for (int i = 0; i < noOfPlayers; i++) {
						String storageName = NewSpdzStorageConstants.STORAGE_NAME_PREFIX
								+ (i + 1);
						String key = NewSpdzStorageConstants.INPUT_KEY_PREFIX
								+ towardsPlayer + "_" + inputCounters[i];
						store.putObject(storageName, key, masks[i]);
						inputCounters[i]++;
					}
				}
			}

			// bits
			int bitCounter = 0;
			for (SpdzSInt[] bit : bits) {
				for (int i = 0; i < noOfPlayers; i++) {
					String storageName = NewSpdzStorageConstants.STORAGE_NAME_PREFIX
							+ (i + 1);
					String key = NewSpdzStorageConstants.BIT_KEY_PREFIX
							+ bitCounter;
					store.putObject(storageName, key, bit[i]);
				}
				bitCounter++;
			}
			
			//exp pipes
			int expCounter = 0;
			for(SpdzSInt[][] expPipe : expPipes) {
				for (int i = 0; i < noOfPlayers; i++) {
					String storageName = NewSpdzStorageConstants.STORAGE_NAME_PREFIX
							+ (i + 1);
					String key = NewSpdzStorageConstants.EXP_PIPE_KEY_PREFIX
							+ expCounter;
					store.putObject(storageName, key, expPipe[i]);
				}
				expCounter++;
			}
		}
	}
}
