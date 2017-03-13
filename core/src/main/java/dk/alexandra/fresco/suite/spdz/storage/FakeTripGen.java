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
package dk.alexandra.fresco.suite.spdz.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.utils.Util;

/**
 * Generates "fake" offline data for SPDZ. I.e. correct offline data generated
 * locally to increase performance.
 * 
 * @author psn
 * 
 */
public class FakeTripGen {

	private static BigInteger mod, alpha;
	private static Random rand;
	private static int size;
	private static int numberOfTriples;
	private static int numberOfParties;
	private static int numberOfBits;
	private static int numberOfInputs;
	private static int numberOfExps;

	private static String triplesFilename = "Triples-p-P";
	private static String expPipeFilename = "Exp-pipe-p-P";
	private static String globalFilename = "Global-data-p-P";
	// private static final String squaresFilename = relativePath +
	// "Squares-p-P";
	private static String inputsFilename = "Inputs-p-P";
	private static String bitsFilename = "Bits-p-P";

	private static final StandardOpenOption WRITE = StandardOpenOption.WRITE;
	private static final StandardOpenOption CREATE = StandardOpenOption.CREATE;

	/**
	 * Generates a byte representation of a SpdzElement (i.e. a share and mac
	 * pair), that is understood by the SpdzByteDataRetreiver.
	 * 
	 * @param element
	 *            the element to be converted.
	 * @return a byte representation of the element.
	 */
	public static ByteBuffer elementToBytes(SpdzElement element) {
		BigInteger share = element.getShare();
		byte[] shareBytes = share.toByteArray();
		BigInteger mac = element.getMac();
		byte[] macBytes = mac.toByteArray();
		byte[] bytes = new byte[size * 2];
		if (shareBytes.length > size) {
			if (shareBytes.length == size + 1) {
				System.arraycopy(shareBytes, 1, bytes, 0, size);
			} else {
				throw new MPCException("This share is too long! Size: "
						+ shareBytes.length);
			}
		} else {
			int length = shareBytes.length;
			System.arraycopy(shareBytes, 0, bytes, size - length, length);
		}
		if (macBytes.length > size) {
			if (macBytes.length == size + 1) {
				System.arraycopy(macBytes, 1, bytes, size, size);
			} else {
				throw new MPCException("This share is too long! Size: "
						+ shareBytes.length);
			}
		} else {
			int length = macBytes.length;
			System.arraycopy(macBytes, 0, bytes, 2 * size - length, length);
		}
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		return bb;
	}

	/**
	 * Generates a byte representation of a BigInteger as understood by the
	 * DataRetreiver.
	 * 
	 * @param b
	 *            a BigInteger.
	 * @return a byte representation.
	 */
	public static ByteBuffer bigIntToBytes(BigInteger b) {
		byte[] bBytes = b.toByteArray();
		byte[] bytes = new byte[size];
		if (bBytes.length > size) {
			if (bBytes.length == size + 1) {
				System.arraycopy(bBytes, 1, bytes, 0, size);
			} else {
				throw new MPCException("This big integer is too long! Size: "
						+ bBytes.length);
			}
		} else {
			int length = bBytes.length;
			System.arraycopy(bBytes, 0, bytes, size - length, length);
		}
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		return bb;
	}

	/**
	 * Generates the given amount of triples. The list contains an array of size
	 * noOfParties - one share for each party
	 * 
	 * @param amount
	 * @param noOfParties
	 * @param modulus
	 * @param alpha
	 * @return
	 */
	public static List<SpdzTriple[]> generateTriples(int amount,
			int noOfParties, BigInteger modulus, BigInteger alpha) {
		FakeTripGen.rand = new Random();
		FakeTripGen.alpha = alpha;
		FakeTripGen.mod = modulus;

		List<SpdzTriple[]> triples = new ArrayList<SpdzTriple[]>(amount);
		for (int i = 0; i < amount; i++) {
			BigInteger a = sample();
			BigInteger macA = getMac(a);
			List<SpdzElement> elementsA = toShares(a, macA, noOfParties);

			BigInteger b = sample();
			BigInteger macB = getMac(b);
			List<SpdzElement> elementsB = toShares(b, macB, noOfParties);

			BigInteger c = b.multiply(a).mod(mod);
			BigInteger macC = getMac(c);
			List<SpdzElement> elementsC = toShares(c, macC, noOfParties);

			SpdzTriple[] arr = new SpdzTriple[noOfParties];
			for (int j = 0; j < elementsA.size(); j++) {
				arr[j] = new SpdzTriple(elementsA.get(j), elementsB.get(j),
						elementsC.get(j));
			}
			triples.add(arr);
		}
		return triples;
	}
	
	/**
	 * Generates the given amount of triples times however many streams are contained in the first list. 
	 * This method writes the triples directly to the given streams.
	 * 
	 * @param amount
	 * @param noOfParties
	 * @param modulus
	 * @param alpha
	 * @param streams the streams to write to. Innermost list should have size of parties, while outermost is per thread used online.  
	 * @throws IOException 
	 */
	public void generateTripleStream(int amount,
			int noOfParties, BigInteger modulus, BigInteger alpha, Random rand, List<List<ObjectOutputStream>> streams) throws IOException {
		FakeTripGen.rand = rand;
		FakeTripGen.alpha = alpha;
		FakeTripGen.mod = modulus;

		for(List<ObjectOutputStream> ooss : streams) {
			for (int i = 0; i < amount; i++) {
				BigInteger a = sample();
				BigInteger macA = getMac(a);
				List<SpdzElement> elementsA = toShares(a, macA, noOfParties);

				BigInteger b = sample();
				BigInteger macB = getMac(b);
				List<SpdzElement> elementsB = toShares(b, macB, noOfParties);

				BigInteger c = b.multiply(a).mod(mod);
				BigInteger macC = getMac(c);
				List<SpdzElement> elementsC = toShares(c, macC, noOfParties);

				for (int j = 0; j < elementsA.size(); j++) {
					ooss.get(j).writeObject(new SpdzTriple(elementsA.get(j), elementsB.get(j),
							elementsC.get(j)));
				}
				if(i % 10000 == 0) {
					for(ObjectOutputStream oos : ooss) {
						oos.flush();
						oos.reset();
					}
				}
			}
		}
	}

	/**
	 * Returns a list of a list of inputmasks. Read as: the innermost array is
	 * as large as noOfParties and contains a sharing of a single inputMask
	 * where player i has the real value. i changes in the outer loop (list).
	 * This means that the inner list contains amount of inputMasks where he
	 * knows the real value
	 * 
	 * @param amount
	 * @param noOfParties
	 * @param modulus
	 * @param alpha
	 * @return
	 */
	public static List<List<SpdzInputMask[]>> generateInputMasks(int amount,
			int noOfParties, BigInteger modulus, BigInteger alpha) {
		FakeTripGen.rand = new Random();
		FakeTripGen.alpha = alpha;
		FakeTripGen.mod = modulus;

		List<List<SpdzInputMask[]>> res = new ArrayList<List<SpdzInputMask[]>>(
				noOfParties);
		for (int currPId = 0; currPId < noOfParties; currPId++) {
			List<SpdzInputMask[]> inputs = new ArrayList<SpdzInputMask[]>(
					amount);
			for (int i = 0; i < amount; i++) {
				BigInteger mask = sample();
				List<SpdzElement> elements = toShares(mask, getMac(mask),
						noOfParties);
				SpdzInputMask[] inputMasks = new SpdzInputMask[noOfParties];
				for (int pId = 0; pId < noOfParties; pId++) {
					SpdzElement elm = elements.get(pId);
					SpdzInputMask inpMask;
					if (pId == currPId) {
						inpMask = new SpdzInputMask(elm, mask);
					} else {
						inpMask = new SpdzInputMask(elm);
					}
					inputMasks[pId] = inpMask;
				}
				inputs.add(inputMasks);
			}
			res.add(inputs);
		}
		return res;
	}
	
	/**
	 * Generates input masks which can be used only if the party inputting has the same ID as the inputterId in this method. 
	 * This streams the data and writes directly to the given object output stream.
	 * @param amount The amount of input masks to generate
	 * @param noOfParties The number of parties in the MPC computation
	 * @param inputterId The ID of the party inputting data
	 * @param modulus The modulus used
	 * @param alpha The secret key used
	 * @param rand The randomness used
	 * @param streams The streams to write to. Innermost list should have size of parties, while outermost is per thread used online.
	 * @throws IOException
	 */
	public void generateInputMaskStream(int amount,
			int noOfParties, int inputterId, BigInteger modulus, BigInteger alpha, Random rand, List<List<ObjectOutputStream>> streams) throws IOException {
		FakeTripGen.rand = rand;
		FakeTripGen.alpha = alpha;
		FakeTripGen.mod = modulus;
		
		for(List<ObjectOutputStream> ooss : streams) {
			for (int i = 0; i < amount; i++) {
				BigInteger mask = sample();
				List<SpdzElement> elements = toShares(mask, getMac(mask),
						noOfParties);
				for (int pId = 0; pId < noOfParties; pId++) {
					SpdzElement elm = elements.get(pId);
					SpdzInputMask inpMask;
					if (pId == inputterId) {
						inpMask = new SpdzInputMask(elm, mask);
					} else {
						inpMask = new SpdzInputMask(elm);
					}
					ooss.get(pId).writeObject(inpMask);
				}
			}
		}
	}

	public static List<SpdzSInt[]> generateBits(int amount, int noOfParties,
			BigInteger modulus, BigInteger alpha) {
		FakeTripGen.rand = new Random();
		FakeTripGen.alpha = alpha;
		FakeTripGen.mod = modulus;

		BigInteger bit;
		List<SpdzSInt[]> res = new ArrayList<SpdzSInt[]>();
		for (int i = 0; i < amount; i++) {
			bit = new BigInteger(1, rand);
			BigInteger mac = getMac(bit);
			List<SpdzElement> elements = toShares(bit, mac, noOfParties);			
			SpdzSInt[] shares = new SpdzSInt[noOfParties];
			for(int j = 0; j < noOfParties; j++) {
				shares[j] = new SpdzSInt(elements.get(j));
			}
			res.add(shares);
		}
		return res;
	}
	
	/**
	 * Writes secret shared bits directly to the given streams. 
	 * @param amount
	 * @param noOfParties
	 * @param modulus
	 * @param alpha
	 * @param rand
	 * @param streams the streams to write to. Innermost list should have size of parties, while outermost is per thread used online.
	 * @throws IOException
	 */
	public void generateBitStream(int amount, int noOfParties,
			BigInteger modulus, BigInteger alpha, Random rand, List<List<ObjectOutputStream>> streams) throws IOException {
		FakeTripGen.rand = rand;
		FakeTripGen.alpha = alpha;
		FakeTripGen.mod = modulus;

		BigInteger bit;
		for(List<ObjectOutputStream> ooss : streams) {
			for (int i = 0; i < amount; i++) {
				bit = new BigInteger(1, rand);
				BigInteger mac = getMac(bit);
				List<SpdzElement> elements = toShares(bit, mac, noOfParties);
				for(int j = 0; j < noOfParties; j++) {
					ooss.get(j).writeObject(new SpdzSInt(elements.get(j)));
				}			
				if(i % 30000 == 0) {
					for(ObjectOutputStream oos : ooss) {
						oos.flush();
						oos.reset();
					}
				}
			}
		}
	}

	/**
	 * Returns a list of double-arrays where the first array contains the expPipe for that player. i.e. list.get(0)[0] contains the expPipe no. 1 for player 1. 
	 * @param amount
	 * @param noOfParties
	 * @param modulus
	 * @param alpha
	 * @return
	 */
	public static List<SpdzSInt[][]> generateExpPipes(int amount, int noOfParties,
			BigInteger modulus, BigInteger alpha) {
		FakeTripGen.rand = new Random();
		FakeTripGen.alpha = alpha;
		FakeTripGen.mod = modulus;

		List<SpdzSInt[][]> res = new ArrayList<SpdzSInt[][]>();
		for (int j = 0; j < amount; j++) {
			SpdzSInt[][] expPipe = new SpdzSInt[noOfParties][Util.EXP_PIPE_SIZE];
			BigInteger r = sample();
			BigInteger rInv = r.modInverse(mod);
			BigInteger mac = getMac(rInv);
			List<SpdzElement> elements = toShares(rInv, mac, noOfParties);
			for(int i = 0; i < noOfParties; i++) {				
				expPipe[i][0] = new SpdzSInt(elements.get(i));
			}

			BigInteger exp = BigInteger.ONE;			
			for (int i = 1; i < Util.EXP_PIPE_SIZE; i++) {
				exp = exp.multiply(r).mod(mod);
				mac = getMac(exp);
				elements = toShares(exp, mac, noOfParties);
				for(int p = 0; p < noOfParties; p++) {
					expPipe[p][i] = new SpdzSInt(elements.get(p));
				}
			}
			res.add(expPipe);
		}
		return res;
	}
	
	public void generateExpPipeStream(int amount, int noOfParties,
			BigInteger modulus, BigInteger alpha, Random rand, List<List<ObjectOutputStream>> streams) throws IOException {
		FakeTripGen.rand = rand;
		FakeTripGen.alpha = alpha;
		FakeTripGen.mod = modulus;

		for(List<ObjectOutputStream> ooss :  streams) {
			for (int j = 0; j < amount; j++) {
				SpdzSInt[][] expPipe = new SpdzSInt[noOfParties][Util.EXP_PIPE_SIZE];
				BigInteger r = sample();
				BigInteger rInv = r.modInverse(mod);
				BigInteger mac = getMac(rInv);
				List<SpdzElement> elements = toShares(rInv, mac, noOfParties);
				for(int i = 0; i < noOfParties; i++) {				
					expPipe[i][0] = new SpdzSInt(elements.get(i));
				}
	
				BigInteger exp = BigInteger.ONE;			
				for (int i = 1; i < Util.EXP_PIPE_SIZE; i++) {
					exp = exp.multiply(r).mod(mod);
					mac = getMac(exp);
					elements = toShares(exp, mac, noOfParties);
					for(int p = 0; p < noOfParties; p++) {
						expPipe[p][i] = new SpdzSInt(elements.get(p));
					}
				}
				for(int i = 0; i < noOfParties; i++) {
					ooss.get(i).writeObject(expPipe[i]);
				}
				if(j % 150 == 0) {
					for(ObjectOutputStream oos : ooss) {						
						oos.flush();
						oos.reset();
					}
				}
			}
		}
	}

	public static List<BigInteger> generateAlphaShares(int noOfParties, BigInteger modulus) {
		FakeTripGen.rand = new Random();
		FakeTripGen.mod = modulus;
		
		List<BigInteger> alphaShares = new ArrayList<BigInteger>();
		BigInteger alphaShare;
		BigInteger lastShare = sample();
		for (int i = 0; i < noOfParties; i++) {
			// Share stuff
			alphaShare = sample();			
			if (i != noOfParties - 1) {
				alphaShares.add(alphaShare);
			} else {
				alphaShares.add(lastShare);
			}
			lastShare = lastShare.subtract(alphaShare).mod(mod);
		}
		return alphaShares;
	}

	/**
	 * Generates offline data and writes it to a file according to the given
	 * arguments. The needed arguments should be explained by running the method
	 * without any arguments. (They included the number of data items to be
	 * produced, where to write the corresponding files and so on).
	 * 
	 * @param args
	 *            arguments to the offline data generator
	 */
	public static void main(String[] args) {
		if (handleArgs(args)) {
			// rand = new SecureRandom();
			rand = new Random(0);
			alpha = sample();
			size = 0;
			byte[] bytes = mod.toByteArray();

			if (bytes[0] == 0) {
				size = mod.toByteArray().length - 1;
			} else {
				size = mod.toByteArray().length;
			}

			bytes = null;
			try {
				System.out.println("START EXP");
				writeExp();
				System.out.println("DONE EXP");
				System.out.println("START TRIPLES");
				writeTriples();
				System.out.println("DONE TRIPLES");
				System.out.println("START INPUTS");
				writeInputs();
				System.out.println("DONE INPUTS");
				System.out.println("START BITS");
				writeBits();
				System.out.println("DONE BITS");
				System.out.println("START GLOBAL");
				writeGlobal();
				System.out.println("DONE GLOBAL");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Handles arguments and writes helpful messages if the arguments are
	 * insufficient.
	 * 
	 * @param args
	 *            the arguments given to the main method.
	 * @return true if the arguments could be parsed, false otherwise.
	 */
	private static boolean handleArgs(String[] args) {
		String primeKey = "-m=";
		boolean primePresent = false;
		String tripKey = "-t=";
		boolean tripPresent = false;
		String inputKey = "-i=";
		boolean inputPresent = false;
		String bitKey = "-b=";
		boolean bitPresent = false;
		String partiesKey = "-p=";
		boolean partiesPresent = false;
		String expKey = "-e=";
		boolean expPresent = false;
		String dirKey = "-d=";
		boolean dirPresent = false;
		String usage = "Please give the following arguments: " + primeKey
				+ "[modulus] " + tripKey + "[#triples] " + inputKey
				+ "[#inputs (per player)] " + bitKey + "[#bits] " + expKey
				+ "[#exp pipes] " + partiesKey + "[#parties] " + dirKey
				+ "[directory (to store files)]";
		for (String arg : args) {
			if (arg.length() < 4) {
				System.err.println("Malformed argument \"" + arg + "\". "
						+ usage);
			}
			String key = arg.substring(0, 3);
			String value = arg.substring(3);
			if (key.equals(primeKey)) {
				mod = new BigInteger(value);
				primePresent = true;
			} else if (key.equals(tripKey)) {
				numberOfTriples = Integer.parseInt(value);
				tripPresent = true;
			} else if (key.equals(inputKey)) {
				numberOfInputs = Integer.parseInt(value);
				inputPresent = true;
			} else if (key.equals(bitKey)) {
				numberOfBits = Integer.parseInt(value);
				bitPresent = true;
			} else if (key.equals(partiesKey)) {
				numberOfParties = Integer.parseInt(value);
				partiesPresent = true;
			} else if (key.equals(expKey)) {
				numberOfExps = Integer.parseInt(value);
				expPresent = true;
			} else if (key.equals(dirKey)) {
				if (value.lastIndexOf("/") != value.length() - 1) {
					value = value + "/";
				}
				inputsFilename = value + inputsFilename;
				bitsFilename = value + bitsFilename;
				triplesFilename = value + triplesFilename;
				globalFilename = value + globalFilename;
				expPipeFilename = value + expPipeFilename;
				dirPresent = true;
			} else {
				System.err.println("Unrecognized argument \"" + arg + "\"."
						+ usage);
			}
		}
		if (!(primePresent && tripPresent && bitPresent && partiesPresent
				&& inputPresent && expPresent && dirPresent)) {
			String missing = usage + "\nThe following arguments were missing: ";
			if (!primePresent) {
				missing += primeKey + "[modulus] ";
			}
			if (!tripPresent) {
				missing += tripKey + "[#triples] ";
			}
			if (!inputPresent) {
				missing += inputKey + "[#inputs] ";
			}
			if (!bitPresent) {
				missing += bitKey + "[#bits] ";
			}
			if (!partiesPresent) {
				missing += partiesKey + "[#parties] ";
			}
			if (!expPresent) {
				missing += expKey + "[#exp pipes] ";
			}
			if (!dirPresent) {
				missing += dirKey + "[directory] ";
			}
			System.err.println(missing);
			return false;
		}
		return true;
	}

	/**
	 * Generates triples and writes them the appropriate file.
	 * 
	 * @throws IOException
	 */
	public static void writeTriples() throws IOException {
		BigInteger a, b, c;
		List<FileChannel> channels = new LinkedList<FileChannel>();
		for (int i = 0; i < numberOfParties; i++) {
			File f = new File(triplesFilename + i);
			if (!f.exists()) {
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			FileChannel fc = fos.getChannel();
			channels.add(fc);
		}
		for (int i = 0; i < numberOfTriples; i++) {
			a = sample();
			b = sample();
			c = b.multiply(a).mod(mod);
			writeAsShared(a, channels);
			writeAsShared(b, channels);
			writeAsShared(c, channels);
		}
		for (FileChannel fc : channels) {
			fc.close();
		}
	}

	/**
	 * Generates a SPDZ sharing (with macs) of a BigInteger value. Then writes
	 * each generated element to a separate file (i.e. one for each party).
	 * 
	 * @param b
	 *            a BigInteger
	 * @param channels
	 *            a FileChannel for each file to write to. This should include a
	 *            channel for each party.
	 * @throws IOException
	 */
	private static void writeAsShared(BigInteger b, List<FileChannel> channels)
			throws IOException {
		BigInteger mac = getMac(b);
		List<SpdzElement> elements = toShares(b, mac, numberOfParties);
		writeElements(elements, channels);
	}

	/**
	 * Generates SPDZ sharing of bits and writes them to the appropriate files.
	 * 
	 * @throws IOException
	 */
	public static void writeBits() throws IOException {
		BigInteger bit;
		List<FileChannel> channels = new LinkedList<FileChannel>();
		for (int i = 0; i < numberOfParties; i++) {
			File f = new File(bitsFilename + i);
			FileOutputStream fos = new FileOutputStream(f);
			FileChannel fc = fos.getChannel();
			channels.add(fc);
		}
		for (int i = 0; i < numberOfBits; i++) {
			bit = new BigInteger(1, rand);
			writeAsShared(bit, channels);
		}
		for (FileChannel fc : channels) {
			fc.close();
		}
	}

	/**
	 * Generates SPDZ sharing of input masks and writes them to the appropriate
	 * files.
	 * 
	 * @throws IOException
	 */
	public static void writeInputs() throws IOException {
		BigInteger mask;
		for (int j = 0; j < numberOfParties; j++) {
			FileChannel selfChannel = null;
			List<FileChannel> channels = new LinkedList<FileChannel>();
			for (int i = 0; i < numberOfParties; i++) {
				File f = new File(inputsFilename + i + '-' + j);
				FileOutputStream fos = new FileOutputStream(f);
				FileChannel fc = fos.getChannel();
				channels.add(fc);
				if (i == j) {
					selfChannel = fc;
				}
			}
			for (int h = 0; h < numberOfInputs; h++) {
				mask = sample();
				List<SpdzElement> elements = toShares(mask, getMac(mask),
						numberOfParties);
				Iterator<SpdzElement> itE = elements.iterator();
				Iterator<FileChannel> itFC = channels.iterator();
				while (itE.hasNext() && itFC.hasNext()) {
					FileChannel fc = itFC.next();
					fc.write(elementToBytes(itE.next()));
					if (fc.equals(selfChannel)) {
						fc.write(bigIntToBytes(mask));
					}
				}
			}
			for (FileChannel fc : channels) {
				if (fc.isOpen()) {
					fc.close();
				}
			}
		}
	}

	/**
	 * Generates the SPDZ global information (modulus and key-share) and writes
	 * to a file. Note: these are in text, not byte format.
	 * 
	 * @throws IOException
	 */
	public static void writeGlobal() throws IOException {
		BigInteger alphaShare;
		BigInteger lastShare = alpha;
		for (int i = 0; i < numberOfParties; i++) {
			// File stuff
			File f = new File(globalFilename + i);
			FileWriter fw = new FileWriter(f);
			// Share stuff
			alphaShare = sample();
			fw.write(mod.toString());
			fw.write(" ");
			if (i != numberOfParties - 1) {
				fw.write(alphaShare.toString());
			} else {
				fw.write(lastShare.toString());
			}
			lastShare = lastShare.subtract(alphaShare).mod(mod);
			fw.close();
		}
	}

	/**
	 * Generates SPDZ sharings of the exp pipes and writes to the appropriate
	 * files.
	 * 
	 * @throws IOException
	 */
	public static void writeExp() throws IOException {
		List<FileChannel> channels = new LinkedList<FileChannel>();
		for (int i = 0; i < numberOfParties; i++) {
			Path expPath = Paths.get(expPipeFilename + i);
			FileChannel fc = FileChannel.open(expPath, WRITE, CREATE);
			channels.add(fc);
		}
		for (int j = 0; j < numberOfExps; j++) {
			BigInteger r = sample();
			BigInteger rInv = r.modInverse(mod);
			writeAsShared(rInv, channels);
			BigInteger exp = BigInteger.ONE;
			for (int i = 1; i < Util.EXP_PIPE_SIZE; i++) {
				exp = exp.multiply(r).mod(mod);
				writeAsShared(exp, channels);
			}
		}
		for (FileChannel fc : channels) {
			fc.close();
		}
	}

	/**
	 * Writes the SpdzElements associated with a SPDZ sharing to the appropriate
	 * files.
	 * 
	 * @param elements
	 *            elements of a SPDZ sharing
	 * @param channels
	 *            channels to the appropriate files (i.e. one for each player)
	 * @throws IOException
	 */
	private static void writeElements(List<SpdzElement> elements,
			List<FileChannel> channels) throws IOException {
		Iterator<SpdzElement> eIt = elements.iterator();
		Iterator<FileChannel> cIt = channels.iterator();
		while (eIt.hasNext() && cIt.hasNext()) {
			cIt.next().write(elementToBytes(eIt.next()));
		}
	}

	/**
	 * Generates the SPDZ sharing of a value and its mac
	 * 
	 * @param value
	 *            the value to share
	 * @param mac
	 *            the mac on the value
	 * @param numberOfParties
	 *            the number of parties
	 * @return a list of SpdzElements giving the SPDZ sharing
	 */
	private static List<SpdzElement> toShares(BigInteger value, BigInteger mac,
			int numberOfParties) {
		List<SpdzElement> elements = new ArrayList<SpdzElement>(numberOfParties);
		BigInteger valShare;
		BigInteger macShare;
		for (int i = 0; i < numberOfParties - 1; i++) {
			valShare = sample();
			macShare = sample();
			value = value.subtract(valShare).mod(mod);
			mac = mac.subtract(macShare).mod(mod);
			elements.add(new SpdzElement(valShare, macShare));
		}
		elements.add(new SpdzElement(value, mac));
		return elements;
	}

	/**
	 * Get a SPDZ mac on a given value
	 * 
	 * @param value
	 *            the value to be maced
	 * @return the mac
	 */
	private static BigInteger getMac(BigInteger value) {
		return value.multiply(alpha).mod(mod);
	}

	/**
	 * Sample a uniformly random integer in the range {0 ... mod}.
	 * 
	 * @return a random integer.
	 */
	private static BigInteger sample() {
		BigInteger result = new BigInteger(mod.bitLength(), rand);
		if (result.compareTo(mod) >= 0) {
			result = null;
			return sample();
		} else {
			return result;
		}
	}
}
