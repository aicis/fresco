/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.spdz.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;

public class InitializeStorage {

	/**
	 * Removes all preprocessed material previously produced by this class'
	 * init*Storage methods.
	 */
	public static void cleanup() throws IOException {
		String folder = SpdzStorageConstants.STORAGE_FOLDER;
		if (!new File(folder).exists()) {
			System.out.println("The folder '" + folder + "' does not exist. Continuing without removing anything");
			return;
		}
		System.out.println("Removing any preprocessed material from the folder " + folder);
		deleteFileOrFolder(Paths.get(folder));
	}

	private static void deleteFileOrFolder(final Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(final Path file, final IOException e) {
				return handleException(e);
			}

			private FileVisitResult handleException(final IOException e) {
				e.printStackTrace(); // replace with more robust error handling
				return FileVisitResult.TERMINATE;
			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
				if (e != null)
					return handleException(e);
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	};

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
	public static void initStorage(Storage[] stores, int noOfPlayers, int noOfTriples, int noOfInputMasks, int noOfBits,
			int noOfExpPipes) {

		List<Storage> tmpStores = new ArrayList<Storage>();
		for (Storage s : stores) {
			if (s.getObject(SpdzStorageConstants.STORAGE_NAME_PREFIX + 1, SpdzStorageConstants.MODULUS_KEY) == null) {
				tmpStores.add(s);
			}
		}
		Storage[] storages = tmpStores.toArray(new Storage[0]);

		BigInteger p = new BigInteger(
				"6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");

		List<BigInteger> alphaShares = FakeTripGen.generateAlphaShares(noOfPlayers, p);
		BigInteger alpha = BigInteger.ZERO;
		for (BigInteger share : alphaShares) {
			alpha = alpha.add(share);
		}
		alpha = alpha.mod(p);

		List<SpdzTriple[]> triples = FakeTripGen.generateTriples(noOfTriples, noOfPlayers, p, alpha);
		List<List<SpdzInputMask[]>> inputMasks = FakeTripGen.generateInputMasks(noOfInputMasks, noOfPlayers, p, alpha);
		List<SpdzSInt[]> bits = FakeTripGen.generateBits(noOfBits, noOfPlayers, p, alpha);
		List<SpdzSInt[][]> expPipes = FakeTripGen.generateExpPipes(noOfExpPipes, noOfPlayers, p, alpha);

		for (Storage store : storages) {
			for (int i = 1; i < noOfPlayers + 1; i++) {
				String storageName = SpdzStorageConstants.STORAGE_NAME_PREFIX + i;
				store.putObject(storageName, SpdzStorageConstants.MODULUS_KEY, p);
				store.putObject(storageName, SpdzStorageConstants.SSK_KEY, alphaShares.get(i - 1));
			}
			// triples
			int tripleCounter = 0;
			for (SpdzTriple[] triple : triples) {
				for (int i = 0; i < noOfPlayers; i++) {
					String storageName = SpdzStorageConstants.STORAGE_NAME_PREFIX + (i + 1);
					store.putObject(storageName, SpdzStorageConstants.TRIPLE_KEY_PREFIX + tripleCounter, triple[i]);
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
						String storageName = SpdzStorageConstants.STORAGE_NAME_PREFIX + (i + 1);
						String key = SpdzStorageConstants.INPUT_KEY_PREFIX + towardsPlayer + "_" + inputCounters[i];
						store.putObject(storageName, key, masks[i]);
						inputCounters[i]++;
					}
				}
			}

			// bits
			int bitCounter = 0;
			for (SpdzSInt[] bit : bits) {
				for (int i = 0; i < noOfPlayers; i++) {
					String storageName = SpdzStorageConstants.STORAGE_NAME_PREFIX + (i + 1);
					String key = SpdzStorageConstants.BIT_KEY_PREFIX + bitCounter;
					store.putObject(storageName, key, bit[i]);
				}
				bitCounter++;
			}

			// exp pipes
			int expCounter = 0;
			for (SpdzSInt[][] expPipe : expPipes) {
				for (int i = 0; i < noOfPlayers; i++) {
					String storageName = SpdzStorageConstants.STORAGE_NAME_PREFIX + (i + 1);
					String key = SpdzStorageConstants.EXP_PIPE_KEY_PREFIX + expCounter;
					store.putObject(storageName, key, expPipe[i]);
				}
				expCounter++;
			}
		}
	}

	/**
	 * Initializes the storage
	 * 
	 * @param streamedStorages
	 *            The storages to initialize (multiple storages are used when
	 *            using a strategy with multiple threads)
	 * @param noOfPlayers
	 *            The number of players
	 * @param noOfThreads
	 *            The number of threads used
	 * @param noOfTriples
	 *            The number of triples to generate
	 * @param noOfInputMasks
	 *            The number of masks for input to generate.
	 * @param noOfBits
	 *            The number of random bits to generate
	 * @param noOfExpPipes
	 *            The number of exponentiation pipes to generate.
	 * @param p
	 *            The modulus to use.
	 */
	public static void initStreamedStorage(FilebasedStreamedStorageImpl storage, int noOfPlayers, int noOfThreads,
			int noOfTriples, int noOfInputMasks, int noOfBits, int noOfExpPipes, BigInteger p) {		
		try {
			// Try get the last thread file. If that fails, we need to
			// generate the files
			if (storage.getNext(SpdzStorageConstants.STORAGE_NAME_PREFIX + noOfThreads + "_" + 1 + "_" + 0 + "_"
					+ SpdzStorageConstants.MODULUS_KEY) != null) {
				return;
			}
		} catch (Exception e) {
			//Likely we could not find the file, so we generate new ones
		}

		System.out.println("Generating preprocessed data!");
		File f = new File("spdz");
		if (!f.exists()) {
			f.mkdirs();
		}

		List<BigInteger> alphaShares = FakeTripGen.generateAlphaShares(noOfPlayers, p);
		BigInteger alpha = BigInteger.ZERO;
		for (BigInteger share : alphaShares) {
			alpha = alpha.add(share);
		}
		alpha = alpha.mod(p);

		FakeTripGen generator = new FakeTripGen();

		for (int i = 1; i < noOfPlayers + 1; i++) {
			for (int threadId = 0; threadId < noOfThreads; threadId++) {
				String storageName = SpdzStorageConstants.STORAGE_NAME_PREFIX + noOfThreads + "_" + i + "_"
						+ threadId + "_";
				storage.putNext(storageName + SpdzStorageConstants.MODULUS_KEY, p);
				storage.putNext(storageName + SpdzStorageConstants.SSK_KEY, alphaShares.get(i - 1));
			}
		}
		System.out.println("Set modulus and alpha. Now generating triples");
		// triples
		List<List<ObjectOutputStream>> streams = new ArrayList<>();
		for (int threadId = 0; threadId < noOfThreads; threadId++) {
			List<ObjectOutputStream> ooss = new ArrayList<ObjectOutputStream>();			
			for (int i = 0; i < noOfPlayers; i++) {			
				String storageName = SpdzStorageConstants.STORAGE_NAME_PREFIX + noOfThreads + "_" + (i + 1)
						+ "_" + threadId + "_"+ SpdzStorageConstants.TRIPLE_STORAGE;
				try {
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(storageName)));
					ooss.add(oos);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new RuntimeException("Could not open the file "+ storageName, e);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("Could not write to the file "+ storageName, e);
				}
			}
			streams.add(ooss);
		}
		//}
		try {
			generator.generateTripleStream(noOfTriples, noOfPlayers, p, alpha, new Random(), streams);
			for(List<ObjectOutputStream> s : streams) {
				for(ObjectOutputStream o : s) {
					o.flush();
					o.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not write the triple stream", e);
		}
		System.out.println("Done generating triples, now generating input masks");

		List<List<List<ObjectOutputStream>>> oosss = new ArrayList<>();		
		for (int towardsPlayer = 1; towardsPlayer < noOfPlayers + 1; towardsPlayer++) {
			streams = new ArrayList<>();
			for (int threadId = 0; threadId < noOfThreads; threadId++) {				
				List<ObjectOutputStream> ooss = new ArrayList<>();
				for (int i = 0; i < noOfPlayers; i++) {
					String storageName = SpdzStorageConstants.STORAGE_NAME_PREFIX + noOfThreads + "_" + (i + 1)
							+ "_" + threadId + "_" + SpdzStorageConstants.INPUT_STORAGE + towardsPlayer;					
					try {
						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(storageName)));
						ooss.add(oos);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						throw new RuntimeException("Could not open the file "+ storageName, e);
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException("Could not write to the file "+ storageName, e);
					}
				}
				streams.add(ooss);
			}
			oosss.add(streams);
		}
		try {
			for (int towardsPlayer = 0; towardsPlayer < noOfPlayers ; towardsPlayer++) {
				generator.generateInputMaskStream(noOfInputMasks, noOfPlayers, towardsPlayer, p, alpha, new Random(), oosss.get(towardsPlayer));				
			}
			for(List<List<ObjectOutputStream>> ooss : oosss) {
				for(List<ObjectOutputStream> s : ooss) {
					for(ObjectOutputStream o : s) {
						o.flush();
						o.close();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not write the Input mask stream", e);
		}

		System.out.println("Done generating input masks, now generating bits");

		streams = new ArrayList<>();
		// bits
		for (int threadId = 0; threadId < noOfThreads; threadId++) {
			List<ObjectOutputStream> ooss = new ArrayList<>();
			for (int i = 0; i < noOfPlayers; i++) {

				String storageName = SpdzStorageConstants.STORAGE_NAME_PREFIX + noOfThreads + "_" + (i + 1)
						+ "_" + threadId + "_"+SpdzStorageConstants.BIT_STORAGE;
				try {
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(storageName)));
					ooss.add(oos);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new RuntimeException("Could not open the file "+ storageName, e);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("Could not write to the file "+ storageName, e);
				}
			}
			streams.add(ooss);
		}
		try {
			generator.generateBitStream(noOfBits, noOfPlayers, p, alpha, new Random(), streams);
			for(List<ObjectOutputStream> s : streams) {
				for(ObjectOutputStream o : s) {
					o.flush();
					o.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not write the bit stream", e);
		}
		
		System.out.println("Done generating bits, now generating exponentiation pipes");

		streams = new ArrayList<>();
		// exp pipes
		for (int threadId = 0; threadId < noOfThreads; threadId++) {
			List<ObjectOutputStream> ooss = new ArrayList<>();
			for (int i = 0; i < noOfPlayers; i++) {			
				String storageName = SpdzStorageConstants.STORAGE_NAME_PREFIX + noOfThreads + "_" + (i + 1)
						+ "_" + threadId + "_" + SpdzStorageConstants.EXP_PIPE_STORAGE;
				try {
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(storageName)));
					ooss.add(oos);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new RuntimeException("Could not open the file "+ storageName, e);
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("Could not write to the file "+ storageName, e);
				}
			}
			streams.add(ooss);
		}
		try {
			generator.generateExpPipeStream(noOfExpPipes, noOfPlayers, p, alpha, new Random(), streams);
			for(List<ObjectOutputStream> s : streams) {
				for(ObjectOutputStream o : s) {
					o.flush();
					o.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not write the exp pipe stream", e);
		}
		System.out.println("Done generating preprocessed data for the SPDZ protocol suite");
	}

	/**
	 * Does the same as
	 * {@link #initStreamedStorage(StreamedStorage, int, int, int, int, int, int, BigInteger)}
	 * but where the chosen modulus is chosen for you, and is the same as the
	 * one found in: {@link DummyDataSupplierImpl}
	 */
	public static void initStreamedStorage(FilebasedStreamedStorageImpl streamedStorage, int noOfPlayers, int noOfThreads,
			int noOfTriples, int noOfInputMasks, int noOfBits, int noOfExpPipes) {
		BigInteger p = new BigInteger(
				"6703903964971298549787012499123814115273848577471136527425966013026501536706464354255445443244279389455058889493431223951165286470575994074291745908195329");
		InitializeStorage.initStreamedStorage(streamedStorage, noOfPlayers, noOfThreads, noOfTriples, noOfInputMasks,
				noOfBits, noOfExpPipes, p);
	}
}
