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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.utils.Util;

/**
 * Retrieves the various forms of preprocessed data used in SPDZ from a series of files. 
 * This implementation works with files of raw byte data, and uses the fast java.nio FileChannel and ByteBuffer 
 * classes to read from disk. This appears to much faster than the previous text-file based retrievers that used
 * BufferedInputStreams and Scanners.  
 * 
 * @author psn
 *
 */
public class SpdzByteDataRetriever implements DataRetriever {
	
	private FileChannel tripleChannel, expChannel, inputChannel, squareChannel, bitChannel;
	private ByteBuffer tripleBuffer, bitBuffer, expBuffer;
	private FileChannel[] otherInputChannels;
	private BigInteger SSK, modulus;
	private final int pID;

	private final int TRIPLE_BUFFER_SIZE = 100;
	private final int EXP_BUFFER_SIZE = 5;
	private final int BIT_BUFFER_SIZE = 100;

	private final String path;
	private final String dataFilename;
	private final String triplesFilename;
	private final String expPipeFilename;
	private final String squaresFilename;
	private final String inputsFilename;
	private final String bitsFilename;
	
	/**
	 * Creates a new retriever. Note to continually retrieve data into memory we use RetrieverThread's. 
	 * 
	 * @param conf the configuration of this party. 
	 * The configuration is mainly used to read the id and number of parties
	 * 
	 * @param triplepath the path to where triples are stored
	 */
	public SpdzByteDataRetriever(int myId, int noOfParties, String triplepath) {
		path = triplepath;
		dataFilename = path+"Global-data-p-P";
		triplesFilename = path+"Triples-p-P";
		expPipeFilename = path+"Exp-pipe-p-P";
		squaresFilename = path+"Squares-p-P";
		inputsFilename = path+"Inputs-p-P";
		bitsFilename = path+"Bits-p-P";
		
		
		this.pID = myId-1;
		int n = noOfParties;
		try {
			tripleChannel = openChannel(triplesFilename+pID);
			expChannel = openChannel(expPipeFilename+pID);
			//squareChannel = openChannel(squaresFilename+pID);
			bitChannel = openChannel(bitsFilename+pID);
			inputChannel = openChannel(inputsFilename+pID+"-"+pID);
			otherInputChannels = new FileChannel[n];
			for(int i = 0; i < n; i++){
				if(i!= pID){
					otherInputChannels[i] = openChannel(inputsFilename+pID+"-"+i);
				}
			}
			Scanner dataInfoReader = new Scanner(openChannel(dataFilename+pID));
			readGlobalData(dataInfoReader);
			dataInfoReader.close();
		} catch (IOException e) {
			throw new MPCException("Could not open all retriever channels. Using path: " + path, e);
		}		
	}
	
	/**
	 * Opens a FileChannel to a file on a given path. 
	 * @param path a file path
	 * @return a FileChannel to the file on the given path.
	 * @throws FileNotFoundException
	 */
	private FileChannel openChannel(String path) throws FileNotFoundException {
		File f = new File(path);
		if (!f.exists()) {
			throw new FileNotFoundException("Could not open file channel to " + f.getAbsolutePath() + ". File does not exists.");
		} else {
			FileInputStream fis = new FileInputStream(f);
			FileChannel channel = fis.getChannel();
			return channel;
		}
	}
	
	/**
	 * Assumes that the file contains two numbers: The modulus as the first number, 
	 * then the Secret shared global key. They must be seperated by a space: " " 
	 */
	private void readGlobalData(Scanner dataInfoReader){
		modulus = new BigInteger(dataInfoReader.next());
		Reporter.info("P"+(pID+1)+": Using modulus: "+modulus);
		SSK = new BigInteger(dataInfoReader.next());
		Reporter.info("P"+(pID+1)+": Using secret key share: " + SSK);
	}
	
	@Override
	public SpdzTriple retrieveTriple() {
		
		// Read triples into the buffer if it has been read or is uninitialized 
		if (tripleBuffer == null || !tripleBuffer.hasRemaining()) {
			// One triple is three shares and three macs 
			int sizeOfTriple = (Util.size)*6; 
			int bytesToRead = sizeOfTriple*TRIPLE_BUFFER_SIZE;
			if (tripleBuffer == null) {
				// A direct buffer should be faster
				tripleBuffer = ByteBuffer.allocateDirect(bytesToRead);
			} else {
				// Not sure if rewind is more appropriate
				tripleBuffer.clear();
			}
			try {
				while (tripleBuffer.hasRemaining()) {
					int bytesRead = tripleChannel.read(tripleBuffer);
					if (bytesRead == -1) {
						//System.err.println("WARNING: Ran out of triples. Resetting triple channel");
						tripleChannel.position(0);
					} 
				}
			} catch (IOException e) {
				throw new MPCException("Could not read triples", e);
			}
			tripleBuffer.flip();
		}
		byte[] array = new byte[Util.size];

		tripleBuffer.get(array);
		BigInteger aShare = new BigInteger(1, array);
		tripleBuffer.get(array);
		BigInteger aMac = new BigInteger(1, array);
		tripleBuffer.get(array);
		BigInteger bShare = new BigInteger(1, array);
		tripleBuffer.get(array);
		BigInteger bMac = new BigInteger(1, array);		
		tripleBuffer.get(array);
		BigInteger cShare = new BigInteger(1, array);
		tripleBuffer.get(array);
		BigInteger cMac = new BigInteger(1, array);		
		SpdzElement cElm = new SpdzElement(cShare, cMac);
		SpdzElement bElm = new SpdzElement(bShare, bMac);
		SpdzElement aElm = new SpdzElement(aShare, aMac);

		SpdzTriple triple = new SpdzTriple(aElm, bElm, cElm);		
		return triple;
	}


	@Override
	public SpdzSInt[] retrieveExpPipe() {
		if (expBuffer == null || !expBuffer.hasRemaining()) {
			// One share and one mac for each value of the exp pipe
			int bytesToRead = Util.size*Util.EXP_PIPE_SIZE*2*EXP_BUFFER_SIZE;
			int totalBytesRead = 0;
			if (expBuffer == null) {
				expBuffer = ByteBuffer.allocateDirect(bytesToRead);
			} else {
				expBuffer.clear();
			}
			try {
				while (totalBytesRead < bytesToRead) {
					int bytesRead = expChannel.read(expBuffer);
					if (bytesRead == -1) {
						//System.err.println("WARNING: Ran out of exp pipes. Resetting exp channel");
						expChannel.position(0);
					} else {
						totalBytesRead += bytesRead;
					}
				}
			} catch (IOException e) {
				throw new MPCException("Could not read exps", e);
			}
			expBuffer.flip();
		}
		byte[] array = new byte[Util.size];
		SpdzSInt[] pipe = new SpdzSInt[Util.EXP_PIPE_SIZE];
		for (int i = 0; i < Util.EXP_PIPE_SIZE; i++) {
			expBuffer.get(array);
			BigInteger share = new BigInteger(1, array);
			expBuffer.get(array);
			BigInteger mac = new BigInteger(1, array);
			SpdzElement pipeElm = new SpdzElement(share, mac);
			pipe[i] = new SpdzSInt(pipeElm);
		}
		return pipe;
	}

	@Override
	public SpdzInputMask retrieveInputMask(int towardPlayerID) {
		int id = towardPlayerID-1;
		BigInteger share;
		BigInteger mac;
		if(id == pID){
			int bytesToRead = Util.size*3;
			int totalBytesRead = 0;
			ByteBuffer bb = ByteBuffer.allocate(bytesToRead);
			try {
				while (totalBytesRead < bytesToRead) {
					int bytesRead = inputChannel.read(bb);
					if (bytesRead == -1) {
						//System.err.println("WARNING: Ran out of inputs. Resetting input channel");
						inputChannel.position(0);
					} else {
						totalBytesRead += bytesRead;
					}
				}
			} catch (IOException e) {
				throw new MPCException("Could not read inputmasks", e);
			}
			byte[] array = new byte[Util.size];
			bb.flip();
			bb.get(array);
			share = new BigInteger(1, array);
			bb.get(array);
			mac = new BigInteger(1, array);
			bb.get(array);
			BigInteger realValue = new BigInteger(1, array);			
			SpdzElement elm = new SpdzElement(share, mac);
			return new SpdzInputMask(elm, realValue);
		}else{
			int bytesToRead = Util.size*2;
			int totalBytesRead = 0;
			ByteBuffer bb = ByteBuffer.allocate(bytesToRead);
			try {
				while (totalBytesRead < bytesToRead) {
					int bytesRead = otherInputChannels[id].read(bb);
					if (bytesRead == -1) {
						//System.err.println("WARNING: Ran out of other inputs. Resetting other input channel");
						otherInputChannels[id].position(0);
					} else {
						totalBytesRead += bytesRead;
					}
				}
			} catch (IOException e) {
				throw new MPCException("Could not read inputmasks", e);
			}
			byte[] array = new byte[Util.size];
			bb.rewind();
			bb.get(array);
			share = new BigInteger(1, array);
			bb.get(array);
			mac = new BigInteger(1, array);
			SpdzElement elm = new SpdzElement(share, mac);
			return new SpdzInputMask(elm);
		}
	}

	@Override
	public SpdzSInt retrieveBit() {
		if (bitBuffer == null || !bitBuffer.hasRemaining()) {
			// One share and one mac for each bit
			int bytesToRead = Util.size*2*BIT_BUFFER_SIZE;
			int totalBytesRead = 0;
			if (bitBuffer == null) {
				bitBuffer = ByteBuffer.allocateDirect(bytesToRead);
			} 	
			bitBuffer.clear();
			try {
				while (totalBytesRead < bytesToRead) {
					int bytesRead = bitChannel.read(bitBuffer);
					if (bytesRead == -1) {
						//System.err.println("WARNING: Ran out of bits. Resetting bit channel");
						bitChannel.position(0);
					} else {
						totalBytesRead += bytesRead;
					}
				}
			} catch (IOException e) {
				throw new MPCException("Could not read bits", e);
			}
			bitBuffer.flip();
		}

		byte[] array = new byte[Util.size];
		bitBuffer.get(array);
		BigInteger share = new BigInteger(1, array);
		bitBuffer.get(array);
		BigInteger mac = new BigInteger(1, array);
		SpdzElement bitElm = new SpdzElement(share, mac);
		SpdzSInt bit = new SpdzSInt(bitElm);
		return bit;
	}


	@Override
	public int getpID() {
		return pID;
	}
	
	/**
	 *  Attempts to do a nice shutdown of the retriever (including closing all filechannels)
	 */
	public void shutdown() {
		try {
			tripleChannel.close();
			//tripleChannel = null;
			//tripleBuffer = null;
			expChannel.close();
			//expChannel = null;
			//expBuffer = null;
			bitChannel.close();
			//bitChannel = null;
			//bitBuffer = null;
			//squareChannel.close();
			inputChannel.close();
			for(int i = 0; i < otherInputChannels.length; i++){
				if(i!= pID){
					otherInputChannels[i].close();
					//otherInputChannels[i] = null;
				}
			}			
		} catch (IOException e) {
			throw new MPCException("Problems closing retriever channels", e);
		}
	}
	
	public BigInteger getModulus() {
		return modulus;
	}
	
	public BigInteger getSSK() {
		return SSK;
	}
}
