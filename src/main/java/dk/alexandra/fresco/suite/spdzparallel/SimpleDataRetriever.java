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
package dk.alexandra.fresco.suite.spdzparallel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.DataRetriever;
import dk.alexandra.fresco.suite.spdz.utils.Util;

/**
 * A simple data retriever. Essentially factoring out the the DataRetriever part of our old spdzretriever (which acts as both
 * retriever and supplier) 
 * @author psn
 *
 */
public class SimpleDataRetriever implements DataRetriever {

	private Scanner myTriplesReader, myExpPipeReader, dataInfoReader, 
	myInputReader, mySquareReader, myBitReader;
	private Scanner[] otherInputReaders;
	private BigInteger SSK, modulus;
	private final int pID;
	
	private static final String relativePath = "triples/spdz2/";
	private static final String dataFilename = relativePath+"Global-data-p-P";
	private static final String triplesFilename = relativePath+"Triples-p-P";
	public static final String expPipeFilename = relativePath+"Exp-pipe-p-P";
	private static final String squaresFilename = relativePath+"Squares-p-P";
	private static final String inputsFilename = relativePath+"Inputs-p-P";
	private static final String bitsFilename = relativePath+"Bits-p-P";

	public SimpleDataRetriever(NetworkConfiguration conf){

		this.pID = conf.getMyId()-1;
		int n = conf.noOfParties(); 
		SSK = null;
		try {													
			myTriplesReader = new Scanner(Util.getInputStream(triplesFilename+pID));
			try{
				myExpPipeReader = new Scanner(Util.getInputStream(expPipeFilename+pID));
			}catch(Exception e){
				System.err.println("Warning: ExpPipe not found. Until then, no real comparisons can be run. " +
						"Consider running the offline second phase to generate the exponentiation pipe");
			}
			try {
				mySquareReader = new Scanner(Util.getInputStream(squaresFilename+pID));
			} catch (IllegalStateException e) {
				Logger.getGlobal().log(Level.WARNING, "Trying to read square file, but resource was not found. Ignoring for now.", e);
			}
			myBitReader = new Scanner(Util.getInputStream(bitsFilename+pID));
			dataInfoReader = new Scanner(Util.getInputStream(dataFilename+pID));

			//Get modulus and SSK into memory
			readGlobalData();

			myInputReader = new Scanner(Util.getInputStream(inputsFilename+pID+"-"+pID));
			otherInputReaders = new Scanner[n];
			for(int i = 0; i < n; i++){
				if(i!= pID){
					otherInputReaders[i] = new Scanner(Util.getInputStream(inputsFilename+pID+"-"+i));
				}
			}
		} catch (FileNotFoundException e) {
			throw new MPCException("One or more files was not found within the path: "+relativePath+". Try running the preprocessing first to generate the file.");
		} catch (IOException e){
			throw new MPCException("Could not process all files. Aborting...");
		}			
	}	

	/**
	 * Assumes that the file contains two numbers: The modulus as the first number, 
	 * then the Secret shared global key. They must be seperated by a space: " " 
	 */
	private void readGlobalData(){
		modulus = new BigInteger(dataInfoReader.next());
		//System.out.println("P"+(pID+1)+": Using modulus: "+modulus);
		SSK = new BigInteger(dataInfoReader.next());
		//System.out.println("P"+(pID+1)+": My share of SSK: " + SSK);
	}


	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.DataRetriever#retrieveTriple()
	 */
	@Override
	public SpdzTriple retrieveTriple(){	
		BigInteger a, mac_a, b, mac_b, c, mac_c;
		try {
			a = new BigInteger(myTriplesReader.next());
			mac_a = new BigInteger(myTriplesReader.next());
			b = new BigInteger(myTriplesReader.next());
			mac_b = new BigInteger(myTriplesReader.next());
			c = new BigInteger(myTriplesReader.next());
			mac_c = new BigInteger(myTriplesReader.next());

			SpdzElement a_elm = new SpdzElement(a, mac_a);
			SpdzElement b_elm = new SpdzElement(b, mac_b);
			SpdzElement c_elm = new SpdzElement(c, mac_c);				

			return new SpdzTriple(a_elm, b_elm, c_elm);
		} catch (NoSuchElementException e) {
			try {
				myTriplesReader = new Scanner(Util.getInputStream(triplesFilename+pID));
				System.err.println("WARNING: Had to reset triple-pipe!");
				return retrieveTriple();
			} catch (IOException e1) {
				throw new MPCException("Could not reset triple-pipe.",e1);
			}				
		}
	}

	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.DataRetriever#retrieveExpPipe()
	 */
	@Override
	public SpdzSInt[] retrieveExpPipe(){
		BigInteger r, mac_r;
		SpdzSInt[] exp_pipe = new SpdzSInt[Util.EXP_PIPE_SIZE];
		for(int i = 0; i < Util.EXP_PIPE_SIZE; i++){
			try{
				r = new BigInteger(myExpPipeReader.next());
				mac_r = new BigInteger(myExpPipeReader.next());
				exp_pipe[i] = new SpdzSInt(new SpdzElement(r, mac_r));					
			}catch(NoSuchElementException e){
				// TODO: this is NOT a good way to handle this! Please do not commit
				try {
					myExpPipeReader = new Scanner(Util.getInputStream(expPipeFilename+pID));
					System.err.println("WARNING: Had to reset the exp-pipe reader!");
					return retrieveExpPipe();
				} catch (IOException e1) {
					throw new MPCException("Could not reset expPipe!", e1);
				}										
			}
		}
		return exp_pipe;
	}

	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.DataRetriever#retrieveInputMask(int)
	 */
	@Override
	public SpdzInputMask retrieveInputMask(int towardPlayerID){
		int otherID = towardPlayerID-1;

		if(otherID == pID){
			BigInteger share = new BigInteger(myInputReader.next());
			BigInteger mac = new BigInteger(myInputReader.next());
			BigInteger realValue = new BigInteger(myInputReader.next());
			SpdzElement elm = new SpdzElement(share, mac);
			return new SpdzInputMask(elm, realValue);
		}else{
			BigInteger share = new BigInteger(otherInputReaders[otherID].next());
			BigInteger mac = new BigInteger(otherInputReaders[otherID].next());
			SpdzElement elm = new SpdzElement(share, mac);
			return new SpdzInputMask(elm);
		}
	}
	
	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.suite.spdz.storage.DataRetriever#retrieveBit()
	 */
	@Override
	public SpdzSInt retrieveBit(){	
		BigInteger b_bit, mac_b_bit;
		try{
			b_bit = new BigInteger(myBitReader.next());
			mac_b_bit = new BigInteger(myBitReader.next());
			SpdzElement b_elm = new SpdzElement(b_bit, mac_b_bit);
			return new SpdzSInt(b_elm);
		} catch(NoSuchElementException e){
			try {
				myBitReader = new Scanner(Util.getInputStream(bitsFilename+pID));
				System.err.println("WARNING: Resetting bit-pipe");
				return retrieveBit();
			} catch (IOException e1) {
				throw new MPCException("Could not re-initialize bit-reader", e1);
			}

		}
	}

	@Override
	public int getpID() {
		return pID;
	}
	
	public BigInteger getModulus(){
		return modulus;			
	}		


	public BigInteger getSSK(){
		return SSK;
	}


}
