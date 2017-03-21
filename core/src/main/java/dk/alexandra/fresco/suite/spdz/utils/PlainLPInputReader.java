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
package dk.alexandra.fresco.suite.spdz.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.LinkedList;

import dk.alexandra.fresco.framework.MPCException;

public class PlainLPInputReader implements LPInputReader {

	private BigInteger[][] constraintValues;
	private BigInteger[] costValues;
	private int[][] constraintPattern;
	private int[] costPattern;
	private int noVariables;
	private int noConstraints;
	private int myId;
	
	private boolean readInputValues;
	private boolean readInputPattern;
	private BufferedReader valuesReader;
	private BufferedReader patternReader;
	

	public PlainLPInputReader(BufferedReader valuesReader, BufferedReader patternReader, int myId) {
		this.valuesReader = valuesReader;
		this.patternReader = patternReader;
		this.myId = myId;
		constraintValues = null;
		costValues = null;
		constraintPattern = null;
		costPattern = null;
		readInputValues = false;
		readInputPattern = false;
		noVariables = -1;
		noConstraints = -1;
	}
	
	public static LPInputReader getStringInputReader(String values, String pattern, int myId) {
		BufferedReader valuesReader = new BufferedReader(new StringReader(values));
		BufferedReader patternReader = new BufferedReader(new StringReader(pattern));
		return new PlainLPInputReader(valuesReader, patternReader, myId);
	}
	
	public static LPInputReader getFileInputReader(File values, File pattern, int myId) throws FileNotFoundException {
		BufferedReader valuesReader = new BufferedReader(new FileReader(values));
		BufferedReader patternReader = new BufferedReader(new FileReader(pattern));
		return new PlainLPInputReader(valuesReader, patternReader, myId);
	}

	@Override
	public BigInteger[][] getConstraintValues() {
		if (readInputValues) {
			return constraintValues;
		} else {
			return null;
		}
	}

	@Override
	public BigInteger[] getCostValues() {
		if (readInputValues) {
			return costValues;
		} else {
			return null;
		}
	}

	@Override
	public BigInteger[] getBValues() {
		BigInteger[] B = null;
		if (readInputValues) {
			B = new BigInteger[constraintValues.length];
			for (int i = 0; i < B.length; i++) {
				BigInteger[] constraint = constraintValues[i];
				B[i] = constraint[constraint.length - 1];
			}
		}
		return B;
	}

	@Override
	public BigInteger[] getFValues() {
		BigInteger[] F = null;
		if (readInputValues) {
			F = new BigInteger[costValues.length];
			for(int i = 0; i < F.length; i++) {
				if (costValues[i] != null) {
					F[i] = costValues[i].negate();
				} else {
					F[i] = null;
				}
			}
		}
		return F;
	}

	@Override
	public BigInteger[][] getCValues() {
		BigInteger [][] C = null;
		if (readInputValues) {
			C = new BigInteger[constraintValues.length][constraintValues[0].length - 1];
			for (int i = 0; i < constraintValues.length; i++) {
				System.arraycopy(constraintValues[i], 0, C[i], 0, C[i].length);
			}
		}
		return C;
	}
	
	@Override
	public int[][] getConstraintPattern() {
		if (readInputPattern) {
			return constraintPattern;
		} else {
			return null;
		}
	}

	@Override
	public int[] getCostPattern() {
		if (readInputPattern) {
			return costPattern;
		} else {
			return null;
		}
	}

	@Override
	public int[] getBPattern() {
		int[] B = null;
		if (readInputPattern) {
			B = new int[noConstraints];
			for (int i = 0; i < B.length; i++) {
				int[] constraint = constraintPattern[i];
				B[i] = constraint[constraint.length - 1];
			}
		}
		return B;
	}

	@Override
	public int[] getFPattern() {
		return getCostPattern();
	}
	
	@Override
	public int[][] getCPattern() {
		int[][] C = null;
		if (readInputPattern) {
			C = new int[noConstraints][noVariables];
			for (int i = 0; i < noConstraints; i++) {
				System.arraycopy(constraintPattern[i], 0, C[i], 0, C[i].length);
			}
		}
		return C;
	}
	
	
	
	@Override
	public void readInput() throws IOException, MPCException {
		readPattern(patternReader);
		readValues(valuesReader);
		checkConsistency();
	}
	
	private void readPattern(BufferedReader patternReader) throws IOException, MPCException {
		if (!readInputPattern) {
			LinkedList<int[]> constraintList = new LinkedList<int[]>();
			String line = patternReader.readLine();
			if (line != null) {
				costPattern = parsePatternLine(line);		
			} else {
				throw new MPCException("Input pattern malformed: Empty input");
			}		
			line = patternReader.readLine();
			while (line != null && !line.trim().equals("")) {
				constraintList.add(parsePatternLine(line));
				line = patternReader.readLine();
			}
			patternReader.close();

			if (noVariables < 0) {
				noVariables = costPattern.length;
			} else if (costPattern.length != noVariables) {
				throw new MPCException("Input malformed: input pattern and values do not match");
			}
			if (noConstraints < 0) {
				noConstraints = constraintList.size();
			} else if (constraintList.size() != noConstraints){
				throw new MPCException("Input malformed: input pattern and values do not match");
			}		
			if (noConstraints == 0) {
				throw new MPCException("Input pattern malformed: No constraints given.");
			}

			int index = 0;
			constraintPattern = new int[noConstraints][noVariables + 1];
			for (int[] row: constraintList) {
				if (row.length != noVariables + 1) {
					throw new MPCException("Input pattern malformed: Dimensions do not match.");
				}
				constraintPattern[index] = row;
				index++;
			}
			readInputPattern = true;
		}
	}

	private int[] parsePatternLine(String line) {
		String[] fields = line.split(",");
		int[] pattern = new int[fields.length];
		for (int i = 0; i < fields.length; i++) {
			pattern[i] = Integer.parseInt(fields[i].trim());
		}
		return pattern;
	}
	
	private void readValues(BufferedReader valueReader) throws IOException, MPCException {
		if (!readInputValues) {
			LinkedList<BigInteger[]> constraintList = new LinkedList<BigInteger[]>();
			String line = valueReader.readLine();
			if (line != null) {
				costValues = parseValueLine(line);		
			} else {
				throw new MPCException("Input values malformed: Empty input");
			}		
			line = valueReader.readLine();
			while (line != null && !line.trim().equals("")) {
				constraintList.add(parseValueLine(line));
				line = valueReader.readLine();
			}
			valueReader.close();

			if (noVariables < 0) {
				noVariables = costValues.length;
			} else if (costValues.length != noVariables) {
				throw new MPCException("Input malformed: input pattern and values do not match");
			}
			if (noConstraints < 0) {
				noConstraints = constraintList.size();
			} else if (constraintList.size() != noConstraints){
				throw new MPCException("Input malformed: input pattern and values do not match");
			}		
			if (noConstraints == 0) {
				throw new MPCException("Input values malformed: No constraints given.");
			}

			int index = 0;
			constraintValues = new BigInteger[noConstraints][noVariables + 1];
			for (BigInteger[] row: constraintList) {
				if (row.length != noVariables + 1) {
					throw new MPCException("Input values malformed: Dimensions do not match " + 
							row.length + " != " + (noVariables + 1));
				}
				constraintValues[index] = row;
				index++;
			}
			readInputValues = true;
		}
	}

	private void checkConsistency() throws MPCException {
		if (readInputValues && readInputPattern) {
			for (int i = 0; i < constraintValues.length; i++) {
				for (int j = 0; j < constraintValues[0].length; j++) {
					if (constraintValues[i][j] == null && 
							(constraintPattern[i][j] == myId || constraintPattern[i][j] == 0)) {
						throw new MPCException("Input malformed: constraint value (" + i + "," + j + ") missing");
					}
				}
			}
			for (int i = 0; i < costValues.length; i++) {
				if (costValues[i] == null && (costPattern[i] == myId || costPattern[i] == 0)) {
					throw new MPCException("Input malformed: cost value " + i + " missing");
				}
			}
		}
	}
	
	private BigInteger[] parseValueLine(String line) {
		String[] fields = line.split(",");
		BigInteger[] values = new BigInteger[fields.length];
		for (int i = 0; i < fields.length; i++) {
			try {
				values[i] = new BigInteger(fields[i].trim());
			} catch (NumberFormatException e){
				values[i] = null;
			}
		}
		return values;
	}

	@Override
	public boolean isRead() {
		return (readInputValues && readInputPattern);
	}

	@Override
	public int getOutputId() {
		// TODO: Read this somehow, for now just output player 1
		return 1;
	}

	
}